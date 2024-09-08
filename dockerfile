FROM openjdk:11

WORKDIR /app
COPY ./src /app/

ENV PORT=9000
EXPOSE ${PORT}

CMD java -cp ./class loadbalancer.WorkServer $PORT