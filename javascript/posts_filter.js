const readline = require('readline');
const fs = require('fs');

const RX_PROPS = new RegExp(/[a-zA-Z]*=".*?"/g);
const NS_PER_SEC = 1e9;
const BLOCK_SIZE = 10000;

// create empty output file
//fs.writeFileSync('./output.xml', '');
fs.writeFileSync('./output1.xml', '');
fs.writeFileSync('./output2.xml', '');

// create line-by-line input stream
const rl = readline.createInterface({
	input: fs.createReadStream('Posts.xml'),
	crlfDelay: Infinity
});

// create counters
let lineCounter = 0;
let blockCounter = 0;

// create timer for block and total
let blockTime = process.hrtime();
var startTime = new Date();




// respond to one line read
rl.on('line', (line) => {
	if (!line.startsWith('  <row')) {
		write(`${line}\n`, 0);
		return;
	}

	let matches = RX_PROPS[Symbol.match](line);
	let str = '';
	let type = 0;
	for (const match of matches) {
		if (!match.startsWith('Body')) {
			str += match + ' '
		}
		if (match === 'PostTypeId="1"') type = 1;
		if (match === 'PostTypeId="2"') type = 2;
	}
	const row = `  <row ${str} />\n`;
	write(row, type);
	
	lineCounter++;
	if (lineCounter % BLOCK_SIZE === 0) {
		blockEnd();
	}

	if (blockCounter === 10) {
		processEnd();
	}
});

// respond to input file end
rl.on('close', () => {
	blockEnd();
	processEnd();
})




// wrapper function for writing
// easy way to switch from file to commandline output
function write(line, type) {
	//console.log(line);
	//fs.appendFileSync('./output.xml', line);
	if (type !== 2) {
		fs.appendFileSync('./output1.xml', line);
	}
	if (type !== 1) {
		fs.appendFileSync('./output2.xml', line);
	}
}

// wrapper function for ending a block
// this just prints info to visualize progress
function blockEnd() {
	blockCounter++;
	const diff = process.hrtime(blockTime);
	const ms = Math.round(diff[0] * NS_PER_SEC + diff[1] / 10000) / 100;
	console.log(`block ${blockCounter} | ${lineCounter} lines | ${ms}`);
	blockTime = process.hrtime();
}

// wrapper function for ending the whole process
// prints complete exec time and then ends
function processEnd() {
	var endTime = Math.round((new Date() - startTime) / 600) / 100;
	console.log(`Execution time: ${endTime} min`);
	process.exit(0);
}