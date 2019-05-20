const readline = require('readline');
const fs = require('fs');

const POST_FILE_PATH = '../PostsWithoutBody.xml';
const COM_FILE_PATH = '../community-posts_bc.csv';
const OUTPUT_FILE_PATH = '../community-tags2.json';

const RX_PROPS = new RegExp(/[a-zA-Z]*=".*?"/g);

// create empty output file
fs.writeFileSync(OUTPUT_FILE_PATH, '');

const postMap = {};
let communityMap = {};
const communityTags = {};

// create line-by-line input stream
const rl1 = readline.createInterface({
	input: fs.createReadStream(COM_FILE_PATH),
	crlfDelay: Infinity
});

console.log('reading communities');
rl1.on('line', (line) => {
	communityMap = JSON.parse(line);
	for (let comm in communityMap) {
		const posts = communityMap[comm];
		for (let i = 0; i < posts.length; i++) {
			postMap[posts[i]] = 1;
		}
	}
});


rl1.on('close', () => {
	console.log('reading posts');
	const rl2 = readline.createInterface({
		input: fs.createReadStream(POST_FILE_PATH),
		crlfDelay: Infinity
	});

	rl2.on('line', (line) => {
		if (!line.startsWith('  <row')) {
			return;
		}

		const matches = RX_PROPS[Symbol.match](line);
		let id = -1;
		let tags = "";
		for (let match of matches) {
			const m = match.split('=');
			if (m[0] === 'Id') {
				id = parseInt(m[1].replace(/"/g, ''));
			} else if (m[0] === 'Tags') {
				tags = m[1];
			}
		}

		if (postMap[id] === 1) {
			postMap[id] = tags.replace(/&lt;/g, '').replace(/"/g, '').split('&gt;');
		}
	});

	rl2.on('close', () => {
		console.log('sorting tags');

		for (let comm in communityMap) {
			const posts = communityMap[comm];
			const tagMap = {};
			const tagList = [];

			for (let post of posts) {
				const tags = postMap[post];
				if (!Array.isArray(tags)) {
					console.log('not array: ', post, tags);
					continue;
				}
				for (let tag of tags) {
					if (tagMap[tag] === undefined) {
						tagMap[tag] = 1;
					} else {
						tagMap[tag] = tagMap[tag] + 1;
					}
				}
			}

			for (let tag in tagMap) {
				const val = tagMap[tag];
				tagList.push({ name: tag, count: val });
			}

			tagList.sort((a, b) => b.count - a.count);

			communityTags[comm] = tagList.slice(0, 10);
		}

		fs.appendFileSync(OUTPUT_FILE_PATH, JSON.stringify(communityTags));
		process.exit(0);
	});

});

