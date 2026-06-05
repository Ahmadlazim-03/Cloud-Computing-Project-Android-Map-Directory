import './globals.css';

export const metadata = {
  title: 'Android Map Directory — API',
  description:
    'REST API backend for the Android Map Directory campus places app.',
};

export default function RootLayout({ children }) {
  return (
    <html lang="id">
      <body>{children}</body>
    </html>
  );
}
