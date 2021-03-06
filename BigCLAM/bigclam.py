import random

import numpy as np
import networkx as nx
from itertools import combinations
import matplotlib.pyplot as plt

'''
Some letters used in the paper:

G: social network
F: community membership strength matrix (rows: nodes / cols: communities)
A: adjacency matrix of the given network G
C: communities
M: edges that represent community affiliations
V: nodes
E: edges

    
Step-by-step:
1. Conductance test(CT), which initializes the affiliation strength matrix
2. Gradient Ascent(GA), which finds the optimal affiliation weights matrix
3. Community Association(CA), which determines if an affiliation exists between a community and a node based on
 the value of affiliation weight recorded under the said matrix in relation to a pre-specified threshold. 

'''

communities = {}
user_tags = []
post_tags = []

test = [
    [0, 1.2, 0, 0.2],
    [0.5, 0, 0, 0.8],
    [0, 1.8, 1.0, 0]
]


# (not used) calculates probability of a connection between two nodes in a graph
def calculate_p(G, u, v):
    first_step = 0
    first_step += np.dot(G[u], G[v])
    second_step = 1 - np.exp(-first_step)
    return second_step


# generates a random community matrix with a number of nodes and communities
def create_random_matrix(node_num, community_num):
    F = np.matrix(np.ones((node_num, community_num)))
    print(np.shape(F))
    for i in range(np.shape(F)[0]):
        for j in range(np.shape(F)[1]):
            F[i, j] = random.random()
    return F


# generates a random community matrix with a number of nodes and communities
def gen_rand_comm(nodes, communities):
    B = np.zeros((nodes, communities))
    for i in range(nodes):
        for j in range(communities):
            prob = random.random()
            if prob > 0.7:
                B[i, j] = prob
    return B


# generates node-to-node adjacency matrix from community matrix
def gen_rand_adjacency(G, N, threshold):
    print(G)
    A = np.zeros((N, N))
    for i in range(len(A)):
        for j in range(len(A)):
            #A[i, j] = 1 - np.exp(-(np.dot(G[i], G[j])))
            prod = 1 - np.exp(-(np.dot(G[i], G[j])))
            #prod = np.dot(G[i], G[j])
            if prod > random.random() and i != j:
                A[i, j] = 1
    return A


# generates a {0,1} adjacency matrix
def gen_adjacency(G, N):
    A = np.zeros((N, N), dtype=np.int8)
    for u in range(np.shape(G)[0]):
        for v in range(np.shape(G)[0]):
            #print((u, v), ">> ", G[u, v])
            if G[u,v] > 0.0:
                A[u,v] = 1
    return A


def create_gephy_graph():
    f = open('jaccard.txt', 'r')
    f2= open("jaccard_gephi.txt","w+")
    user_to_user = eval(f.read())
    f.close()
    B = nx.from_dict_of_dicts(user_to_user)
    for line in B:
        for item in B[line]:
            my_str = str(line) + ", "
            my_str += str(item) + ", "
            my_str += str(B[line][item]["weight"])
        f2.write(my_str + "\n")
    f2.close()






# check if a matrix is symmetric
def transpose(mat, tr, N):
    for i in range(N):
        for j in range(N):
            tr[i][j] = mat[j][i]

def isSymmetric(mat, N):
    tr = [[0 for j in range(len(mat[0]))] for i in range(len(mat))]
    transpose(mat, tr, N)
    for i in range(N):
        for j in range(N):
            if (mat[i][j] != tr[i][j]):
                return False
    return True







def create_node_adj():
    f = open('jaccard005.txt', 'r')
    user_to_user = eval(f.read())
    f.close()
    B = nx.from_dict_of_dicts(user_to_user)
    C = nx.to_numpy_matrix(B)

    m_size = len(B)
    A = np.zeros((m_size, m_size))
    """
    for u, line in enumerate(B):
        user_tags.append(line)
        for v, item in enumerate(B[line]):
            weight = B[line][item]["weight"]
        if weight > 0.0 and u != v:
            #print(u,v, weight)
            A[u, v] = 1
            A[v, u] = 1
    return A
    """
    for num, i in enumerate(B):
        user_tags.append(i)

    A = A + (C + C.T)/2
    A = np.where(A > 0.0, 1, 0)

    return A


#create_node_adj()



def create_post_adj():
    f = open('jaccard_post.tsv', 'r')
    nodesu = {}
    for num, line in enumerate(f):
        if (num != 0):
            line = line.replace("\n", "").split("\t")
            curr_node = int(line[0])
            other_node = int(line[1])
            if curr_node not in nodesu:
                nodesu[curr_node] = []
            nodesu[curr_node].append(other_node)
    f.close()

    B = nx.from_dict_of_lists(nodesu)
    C = nx.to_numpy_matrix(B)

    m_size = len(B)
    A = np.zeros((m_size, m_size))

    for num, i in enumerate(B):
        post_tags.append(i)

    A = A + (C + C.T) / 2
    A = np.where(A > 0.0, 1, 0)

    #print(isSymmetric(A, m_size))

    return A


create_post_adj()







def sigm(x):
    return np.divide(np.exp(-1. * x), 1. - np.exp(-1. * x))


def log_likelihood(F, A):
    """
    implements equation 2 of
    https://cs.stanford.edu/people/jure/pubs/bigclam-wsdm13.pdf
    """
    A_soft = F.dot(F.T)

    # Next two lines are multiplied with the adjacency matrix, A
    # A is a {0,1} matrix, so we zero out all elements not contributing to the sum
    FIRST_PART = A * np.log(1. - np.exp(-1. * A_soft))
    sum_edges = np.sum(FIRST_PART)
    SECOND_PART = (1 - A) * A_soft
    sum_nedges = np.sum(SECOND_PART)

    log_l = sum_edges - sum_nedges
    return log_l


def gradient(F, A, u):
    """
    The longest step in the process
    Implements equation 3 of
    https://cs.stanford.edu/people/jure/pubs/bigclam-wsdm13.pdf

      * u indicates the row under consideration
    """
    N, C = F.shape

    neighbours = np.where(A[u])
    nneighbours = np.where(1 - A[u])

    sum_nneigh = np.zeros((C,))
    # Speed up this computation using eq.4
    for nnb in nneighbours[0]:
        sum_nneigh += F[nnb]

    sum_neigh = np.zeros((C,))
    for nb in neighbours[0]:
        dotproduct = F[nb].dot(F[u])
        sum_neigh += F[nb] * sigm(dotproduct)

    grad = sum_neigh - sum_nneigh
    return grad


def find_f(A, C, iterations=10):
    """
    The BigClam algorithm
    :param A: Adjacency matrix
    :param C: number of communities
    :param iterations: (optional) when fitting, set a limit to how many iterations the algorithm should do
    :return: Affiliation matrix
    """
    # initialize an F
    N = A.shape[0]
    F = np.random.rand(N, C)

    n = 0
    prevll = 0
    # 1. compute gradient of row u
    while True:
    #for n in range(iterations):
        last_F = F.copy()
        for person in range(N):
            grad = gradient(F, A, person)
            # 2. update the row
            F[person] += 0.005 * grad
            # 3. Project Fu back to a non-negative vector
            F[person] = np.maximum(0.00000001, F[person])  # F should be nonnegative
        #ll = log_likelihood(F, A)
        n += 1
        f_norm = np.sum(np.multiply(F - last_F, F - last_F)) ** 0.5

        print('At step %5i/%5i Frobenius: %5.3f' % (n, iterations, f_norm))

        if n >= iterations:
            break
        """
        print('At step %5i/%5i ll is %5.3f / %5.3f Frobenius: %5.3f' % (n, iterations, ll, np.abs((prevll-ll)/ll), f_norm))
        if np.abs((prevll-ll)/ll) < 0.01 and n > iterations:
            break
        prevll = ll
        """
    return F


def gen_graph(F, Fmax):
    """
    Generate the graph so it can be drawn
    :param F: fitted preferential matrix after bigClam has been run
    :param Fmax: Preferred communities of nodes in F
    """
    print("Generating graph...")
    # create an empty graph
    colors = ['red',
              'green',
              'blue',
              'indigo',
              'yellow',
              'cyan',
              'magenta',
              'darkgreen',
              'navy',
              'pink',
              'darkcyan',
              'deeppink',
              'crimson',
              'orange',
              'lime',
              'brown',
              ]
    print("Len colors: ", len(colors))
    G = nx.Graph()
    # add nodes into the graph, shape(F)[0] is the # of columns of F
    G.add_nodes_from(range(np.shape(F)[0]))
    # generate edges
    max_p = 0

    for pairs in combinations(G.nodes(), 2):
        [u, v] = pairs
        # create links

        prob = 1 - np.exp(-F[u,:]*F[v,:])
        for p in prob:
            if p >= random.random():
                if Fmax[u] == Fmax[v]:
                    G.add_edge(u,v, color=colors[Fmax[u]])
                else:
                    G.add_edge(u, v, color='whitesmoke')

    print("Max P:", max_p)
    return G


'''
Create random graph and bigClam it
'''
def random_main():
    num_of_comms = 5
    # create social networkB

    B = gen_rand_comm(1000, num_of_comms)

    # from B, create G, or adjacency network
    adj = gen_rand_adjacency(B, len(B), 0.8)
    print(adj)
    F = find_f(adj, num_of_comms, 10)
    print(F)
    # np.argmax finds the node's highest community
    F_max = np.argmax(F, 1)
    #print("FMAX: ", F_max, " length: ", len(F_max))
    f_graph = gen_graph(F, F_max)
    edges = f_graph.edges()
    colors = [f_graph[u][v]['color'] for u, v in edges]
    nx.draw(f_graph, node_size=1, width=0.1, font_size=0.5, edge_color=colors,
            pos=nx.spring_layout(f_graph, iterations=20))
    plt.savefig('fig.svg')
    plt.show()


def write_to_file_nodes(data):
    f = open('big_clam_users.txt', 'w')
    f.write("{\n")
    for num, line in enumerate(data.values()):
        new_line = '"' + str(num) + '"' + ":" + str(line) + ",\n"
        f.write(new_line.replace("\'", "\""))
    f.write("}\n")
    f.close()

def write_to_file_posts(data):
    f = open('big_clam_posts.txt', 'w')
    f.write("{\n")
    for num, line in enumerate(data.values()):
        new_line = '"' + str(num) + '"' + ":" + str(line) + ",\n"
        f.write(new_line.replace("\'", "\""))
    f.write("}\n")
    f.close()


def node_to_node(num_communities, num_steps):
    num_of_comms = num_communities
    # create social networkB

    for i in range(num_of_comms):
        communities[i] = []


    adj = create_node_adj()
    print(adj)
    F = find_f(adj, num_of_comms, num_steps)
    print(F)
    # np.argmax finds the node's highest community
    F_max = np.argmax(F, 1)
    print("FMAX: ", F_max, " length: ", len(F_max))

    for user, c in enumerate(F_max):
        communities[c].append(str(user_tags[user]))

    write_to_file_nodes(communities)

    f_graph = gen_graph(F, F_max)
    print("Graph data:")
    print(nx.info(f_graph))

    edges = f_graph.edges()
    colors = [f_graph[u][v]['color'] for u, v in edges]
    nx.draw(f_graph, node_color="black", node_size=0.2, width=0.1, font_size=0.5, edge_color=colors,
            pos=nx.kamada_kawai_layout(f_graph))
    plt.savefig('node-to-node.png', quality=100)
    plt.show()


def post_to_post(num_communities, num_steps):
    num_of_comms = num_communities
    # create social networkB

    for i in range(num_of_comms):
        communities[i] = []

    adj = create_post_adj()
    print(adj)
    F = find_f(adj, num_of_comms, num_steps)
    print(F)
    # np.argmax finds the node's highest community
    F_max = np.argmax(F, 1)
    for post, c in enumerate(F_max):
        communities[c].append(str(post_tags[post]))

    write_to_file_posts(communities)

    print("FMAX: ", F_max, " length: ", len(F_max))
    f_graph = gen_graph(F, F_max)
    print("Graph data:")
    print(nx.info(f_graph))

    edges = f_graph.edges()
    colors = [f_graph[u][v]['color'] for u, v in edges]
    nx.draw(f_graph, node_color="black", node_size=0.2, width=0.1, font_size=0.5, edge_color=colors,
            pos=nx.kamada_kawai_layout(f_graph))
    """
    nx.draw(f_graph, node_color="black", node_size=0.2, width=0.1, font_size=0.5, edge_color=colors,
            pos=nx.spring_layout(f_graph, iterations=200))
    """
    plt.savefig('post-to-post.png', quality=95)
    plt.show()


node_to_node(15, 500)
#post_to_post(7, 400)


