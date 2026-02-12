const fs = require('fs/promises');
const path = require('path');

async function copyDir(src, dest) {
  await fs.mkdir(dest, { recursive: true });
  const entries = await fs.readdir(src, { withFileTypes: true });

  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);

    if (entry.isDirectory()) {
      await copyDir(srcPath, destPath);
    } else {
      await fs.copyFile(srcPath, destPath);
    }
  }
}

async function main() {
  const root = path.join(__dirname, '..');
  const src = path.join(root, 'src');
  const dist = path.join(root, 'dist');

  await fs.rm(dist, { recursive: true, force: true });
  await copyDir(src, dist);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
