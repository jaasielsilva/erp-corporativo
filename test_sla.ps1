# Script para testar endpoint de métricas SLA
Write-Host "Testando endpoint de métricas SLA..." -ForegroundColor Green

# Criar sessão web
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

try {
    # Fazer login
    Write-Host "Fazendo login..." -ForegroundColor Yellow
    $loginData = @{
        username = 'master@sistema.com'
        password = 'master123'
    }
    
    $loginResponse = Invoke-WebRequest -Uri 'http://localhost:8080/login' -Method POST -Body $loginData -SessionVariable session -UseBasicParsing
    Write-Host "Login realizado com sucesso!" -ForegroundColor Green
    
    # Testar endpoint para período geral (0 dias)
    Write-Host "Testando métricas SLA para período geral..." -ForegroundColor Yellow
    $metricsResponse = Invoke-WebRequest -Uri 'http://localhost:8080/suporte/api/metricas-sla-periodo?dias=0' -Method GET -WebSession $session -UseBasicParsing
    
    Write-Host "Resposta do endpoint:" -ForegroundColor Cyan
    Write-Host $metricsResponse.Content -ForegroundColor White
    
    # Testar endpoint para 7 dias
    Write-Host "`nTestando métricas SLA para 7 dias..." -ForegroundColor Yellow
    $metrics7Response = Invoke-WebRequest -Uri 'http://localhost:8080/suporte/api/metricas-sla-periodo?dias=7' -Method GET -WebSession $session -UseBasicParsing
    
    Write-Host "Resposta do endpoint (7 dias):" -ForegroundColor Cyan
    Write-Host $metrics7Response.Content -ForegroundColor White
    
} catch {
    Write-Host "Erro: $($_.Exception.Message)" -ForegroundColor Red
}