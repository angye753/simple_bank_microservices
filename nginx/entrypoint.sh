#!/bin/sh
set -e

SSL_DIR=/etc/nginx/ssl
mkdir -p $SSL_DIR

IP_ADDR="87.212.222.222"

if [ ! -f "$SSL_DIR/server.crt" ]; then
  echo "Generating self-signed certificate for IP: $IP_ADDR ..."

  cat > $SSL_DIR/ip_cert.conf <<EOF
[req]
default_bits       = 2048
prompt             = no
default_md         = sha256
req_extensions     = req_ext
distinguished_name = dn

[dn]
CN = $IP_ADDR

[req_ext]
subjectAltName = @alt_names

[alt_names]
IP.1 = $IP_ADDR
EOF

  openssl req -x509 -nodes -days 365 \
    -newkey rsa:2048 \
    -keyout $SSL_DIR/server.key \
    -out $SSL_DIR/server.crt \
    -config $SSL_DIR/ip_cert.conf
fi

echo "Starting NGINX with HTTPS..."
exec nginx -g "daemon off;"