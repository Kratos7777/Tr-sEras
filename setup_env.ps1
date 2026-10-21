# ThreeEras — Setup do ambiente de desenvolvimento (Windows / PowerShell)
Write-Host "Criando ambiente virtual (.venv)..." -ForegroundColor Cyan
python -m venv .venv

Write-Host "Ativando ambiente virtual e instalando dependências..." -ForegroundColor Cyan
& ".\.venv\Scripts\Activate.ps1"
python -m pip install --upgrade pip
pip install -r requirements.txt

Write-Host "Ambiente configurado com sucesso!" -ForegroundColor Green