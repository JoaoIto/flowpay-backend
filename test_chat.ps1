$headers = @{
    "Content-Type" = "application/json"
}
$body = @{
    "customerId" = "+5511999999999"
    "teamType" = "CARTOES"
    "channel" = "WHATSAPP"
    "subject" = "Gostaria de falar sobre meu cartao"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/chats" -Method Post -Headers $headers -Body $body
    Write-Output "Success!"
} catch {
    Write-Output "Error: $($_.Exception.Message)"
    if ($_.ErrorDetails) {
        Write-Output "Details: $($_.ErrorDetails.Message)"
    }
    
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $responseBody = $reader.ReadToEnd()
    Write-Output "Response body: $responseBody"
}
