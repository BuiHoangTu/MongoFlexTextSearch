# Usage notice 
## Prepare environment variables 
- MONGODB_HOST: host of mongodb 
- MONGODB_USERNAME: username of mongodb
- MONGODB_PASSWORD: password of mongodb
- MONGODB_PORT: port of mongodb
- MONGODB_AUTH_DB: authenticate database of mongodb
- DATABASE_PATH: where the mongodb dump data located

### Using environment file 
1. create file which contains not existed or need to override environment variables
2. run command
``bash
   source ./export_env.sh [environment-file]
``
. If not specified, environment-file = ./.env