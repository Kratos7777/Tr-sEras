#!/usr/bin/env bash
echo "Criando ambiente virtual (.venv)..."
python3 -m venv .venv

echo "Ativando ambiente virtual e instalando dependências..."
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt

echo "Ambiente configurado com sucesso!"