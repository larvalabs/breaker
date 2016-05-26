export default function formatBytes(bytes, decimals) {
  if (bytes === 0) return '0 Byte';
  const k = 1000;
  const dm = decimals + 1 || 3;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  const sizeFloat = parseFloat((bytes / Math.pow(k, i)).toFixed(dm));
  const sizeUnit = sizes[i];
  return `${sizeFloat} ${sizeUnit}`;
}
