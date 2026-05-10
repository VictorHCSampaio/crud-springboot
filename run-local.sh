#!/bin/zsh
# Script para carregar variáveis de ambiente do arquivo .env e executar o Maven

# Carrega as variáveis do arquivo .env
if [ -f .env ]; then
    export $(cat .env | xargs)
    echo "✅ Variáveis de ambiente carregadas do arquivo .env"
else
    echo "❌ Arquivo .env não encontrado. Copie .env.example para .env e configure suas credenciais."
    exit 1
fi

# Executa o Maven com o perfil local
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"


