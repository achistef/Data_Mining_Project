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


test = [
    [0, 1.2, 0, 0.2],
    [0.5, 0, 0, 0.8],
    [0, 1.8, 1.0, 0]
]


# generates a random community matrix with a number of nodes and communities
def create_random_matrix(node_num, community_num):
    F = np.matrix(np.ones((node_num, community_num)))
    print(np.shape(F))
    for i in range(np.shape(F)[0]):
        for j in range(np.shape(F)[1]):
            F[i, j] = random.random()
    return F


# generates a random community matrix with a number of nodes and communities
def gen_comm(nodes, communities):
    B = np.zeros((nodes, communities))
    for i in range(nodes):
        for j in range(communities):
            prob = random.random()
            if prob > 0.85:
                B[i, j] = prob
    return B


# generates node-to-node adjacency matrix from community matrix
def gen_adjacency(G, N, threshold):
    A = np.zeros((N, N), dtype=np.int8)
    for i in range(len(A)):
        for j in range(len(A)):
            prod = 1 - np.exp(-(np.dot(G[i], G[j])))
            if prod > threshold and i != j:
                A[i, j] = 1
    return A


# (not used) calculates probability of a connection between two nodes in a graph
def calculate_p(G, u, v):
    first_step = 0
    first_step += np.dot(G[u], G[v])
    second_step = 1 - np.exp(-first_step)
    return second_step





def sigm(x):
    return np.divide(np.exp(-1. * x), 1. - np.exp(-1. * x))


def log_likelihood(F, A):
    """implements equation 2 of 
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
    """Implements equation 3 of
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
    # initialize an F
    N = A.shape[0]
    F = np.random.rand(N, C)

    # 1. compute gradient of row u
    for n in range(iterations):
        for person in range(N):
            grad = gradient(F, A, person)
            # 2. update the row
            F[person] += 0.005 * grad

            # 3. Project Fu back to a non-negative vector
            F[person] = np.maximum(0.001, F[person])  # F should be nonnegative
        #ll = log_likelihood(F, A)
        #print('At step %5i/%5i ll is %5.3f' % (n, iterations, ll))
        print('At step %5i/%5i' % (n, iterations))
    return F


def gen_graph(F):
    # create an empty graph
    G = nx.Graph()
    # add nodes into the graph, shape(F)[0] is the # of columns of F
    G.add_nodes_from(range(np.shape(F)[0]))
    # generate edges
    for pairs in combinations(G.nodes(), 2):
        [u, v] = pairs
        # create links
        prob = 1 - np.exp(np.dot(-F[u],F[v]))
        if prob >= random.random():
            G.add_edge(u, v)
    return G


def main():
    num_of_comms = 4
    # create social networkB

    B = gen_comm(500, num_of_comms)

    # from B, create G, or adjacency network
    adj = gen_adjacency(B, len(B), 0.1)
    print(adj)
    F = find_f(adj, num_of_comms, 100)
    print(F)
    # np.argmax finds the node's highest community
    F_max = np.argmax(F, 1)
    #print("FMAX: ", F_max, " length: ", len(F_max))
    f_graph = gen_graph(F)
    nx.draw_networkx(f_graph, with_labels=False, node_size=500)
    plt.savefig('fig.png')
    plt.show()


main()