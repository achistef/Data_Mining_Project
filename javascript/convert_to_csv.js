const readline = require('readline');
const fs = require('fs');

const INPUT_FILE_PATH = '../jaccard005.txt';
const OUTPUT_FILE_PATH = '../jaccard005.csv';

// create empty output file
fs.writeFileSync(OUTPUT_FILE_PATH, 'source, target, weight\n');

// create line-by-line input stream
const rl = readline.createInterface({
	input: fs.createReadStream(INPUT_FILE_PATH),
	crlfDelay: Infinity
});

rl.on('line', (line) => {
	let text = line;
	text = text.replace(/'/g, '"');
	text = text.replace(/(?:\d|\.)+/g, (x) => `"${x}"`);
	const edgeObj = JSON.parse(text);

	for (let nodeA in edgeObj) {
		const val = edgeObj[nodeA];
		const nodeAint = parseInt(nodeA);

		for (let nodeB in val) {
			const nodeBint = parseInt(nodeB);

			if (nodeAint < nodeBint) {
				const weight = val[nodeB].weight;
				fs.appendFileSync(OUTPUT_FILE_PATH, `${nodeA}, ${nodeB}, ${weight}\n`);
			}
		}
	}
});

rl.on('close', () => {
	process.exit(0);
});