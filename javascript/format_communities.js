const readline = require('readline');
const fs = require('fs');

const COMMUNITIES_FILE_PATH = '../jaccard005-communities.csv';
const OUTPUT_FILE_PATH = '../communities-ordered.csv';

// create empty output file
fs.writeFileSync(OUTPUT_FILE_PATH, 'communityId; users\n');

const communityMap = {};

// create line-by-line input stream
const rl = readline.createInterface({
	input: fs.createReadStream(COMMUNITIES_FILE_PATH),
	crlfDelay: Infinity
});

rl.on('line', (line) => {
	if (!line.startsWith('Id')) {
		const data = line.split(',,,');
		const userId = data[0];
		const communityId = data[1];

		if (communityMap[communityId] == null) {
			communityMap[communityId] = [];
		}

		communityMap[communityId].push(userId);
	}
});

setTimeout(() => {
	for (let commId in communityMap) {
		const commList = communityMap[commId];
		fs.appendFileSync(OUTPUT_FILE_PATH, `${commId}; ${JSON.stringify(commList)}\n`);
	}
}, 1000);



// rl.on('close', () => {
// 	process.exit(0);
// });