const readline = require('readline');
const fs = require('fs');

//const COM_FILE_PATH = '../communities-ordered.csv';
const COM_FILE_PATH = '../big_clam_users3.txt';
const U2Q_FILE_PATH = '../users-to-qid.csv';
const OUTPUT_FILE_PATH = '../community-posts_bc.csv';

// create empty output file
fs.writeFileSync(OUTPUT_FILE_PATH, '');

let communityMap = {};
const usersMap = {};

// create line-by-line input stream
const rl1 = readline.createInterface({
	input: fs.createReadStream(COM_FILE_PATH),
	crlfDelay: Infinity
});

rl1.on('line', (line) => {
	// const rows = line.split(';');
	// const comId = rows[0];
	// if (comId !== 'communityId') {
	// 	let userIds = JSON.parse(rows[1]);
	// 	userIds = userIds.map((x) => { return parseInt(x); });
	// 	communityMap[comId] = userIds;
	// }
	communityMap = JSON.parse(line);
});


rl1.on('close', () => {
	const rl2 = readline.createInterface({
		input: fs.createReadStream(U2Q_FILE_PATH),
		crlfDelay: Infinity
	});

	rl2.on('line', (line) => {
		let rows = line.split(',');
		const userId = rows.shift();
		if (userId !== 'UserId') {
			const userMap = {};
			for (let i = 0; i < rows.length; i++) {
				const qid = rows[i].replace('"[', '').replace(']"','').trim();
				userMap[qid] = 1;
			}
			usersMap[userId] = userMap;
		}
	});

	rl2.on('close', () => {
		const communitiesPosts = {};

		// every community
		for (let comId in communityMap) {
			const communityUsers = communityMap[comId];
			const communityPostsMap = {};

			// every user pair of communtiy
			for (let i = 0; i < communityUsers.length; i++) {
				for (let j = i+1; j < communityUsers.length; j++) {
					const userA = communityUsers[i];
					const userB = communityUsers[j];

					const userAposts = usersMap[userA];
					const userBposts = usersMap[userB];
					//const bothPosts = [];
					if (userAposts === undefined || userBposts === undefined) {
						continue;
					}

					// find post intersection of both users
					for (let userApost in userAposts) {
						if (userBposts[userApost] === 1) {
							//bothPosts.push(userApost);
							communityPostsMap[userApost] = 1;
						}
					}
				}
			}
			communitiesPosts[comId] = Object.keys(communityPostsMap);
		}



		fs.appendFileSync(OUTPUT_FILE_PATH, JSON.stringify(communitiesPosts));
		process.exit(0);
	})
});

