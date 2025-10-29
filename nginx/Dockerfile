FROM nginx:latest

# Copy custom config and entrypoint
COPY conf.d /etc/nginx/conf.d
COPY entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]