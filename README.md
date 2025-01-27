# SSH-Game
An implementation of Tic-Tac-Toe (and possibly other games in the future) over SSH.

## ⚠️ Disclaimer ⚠️
This project is still under heavy development and is not yet ready for production use.
Since I don't expect anyone to use this project yet, I won't be providing database migrations to future versions.

## Setup
### Docker Compose
First download the latest `docker-compose.yml`
```bash
wget https://raw.githubusercontent.com/Byteingpython/ssh-game/refs/heads/master/docker-compose.yml
```
Now adjust the environment variables in the `docker-compose.yml` file to your liking and **set a better SurrealDB password!**
Then run the following command to start the server:
```bash
docker compose up -d
``` 

### Generating the host keypair
To stop SSH clients from complaining the host keypair of the SSH-Server has to stay consistent. To achieve this you have to set the environment variables `SSH_PRIVATE_KEY` and `SSH_PUBLIC_KEY`.

First generate the keys:
```bash
openssl genrsa -out private_key.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der
```
Then output the keys in the required format. 
```bash
base64 -w 0 private_key.der 
base64 -w 0 public_key.der
```
Now set `SSH_PRIVATE_KEY` and `SSH_PUBLIC_KEY` to these values.