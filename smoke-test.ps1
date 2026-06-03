# Smoke test script for UrbanMicro-CAD backend API
$ErrorActionPreference = "Stop"

Write-Host "=== 1. Register ==="
$regBody = '{"username":"smoketest","password":"smoke123456","email":"smoke@test.com"}'
try {
    $reg = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/register' -Method Post -ContentType 'application/json' -Body $regBody
    Write-Host "REGISTER OK: $($reg | ConvertTo-Json -Compress)"
} catch {
    $status = $_.Exception.Response.StatusCode.value__
    $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
    $body = $reader.ReadToEnd()
    Write-Host "REGISTER (status=$status): $body"
}

Write-Host "`n=== 2. Login ==="
$loginBody = '{"username":"smoketest","password":"smoke123456"}'
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body $loginBody
$token = $login.data.token
Write-Host "LOGIN OK - token length: $($token.Length), userId: $($login.data.userId)"

$headers = @{ Authorization = "Bearer $token" }

Write-Host "`n=== 3. Me ==="
$me = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/me' -Method Get -Headers $headers
Write-Host "ME OK: username=$($me.data.username), role=$($me.data.role)"

Write-Host "`n=== 4. Create Project ==="
$createBody = '{"name":"Smoke Test Project","description":"Created by smoke test"}'
$proj = Invoke-RestMethod -Uri 'http://localhost:8080/api/projects' -Method Post -ContentType 'application/json' -Headers $headers -Body $createBody
$projectId = $proj.data.id
Write-Host "CREATE OK: id=$projectId, name=$($proj.data.name)"

Write-Host "`n=== 5. List Projects ==="
$list = Invoke-RestMethod -Uri 'http://localhost:8080/api/projects' -Method Get -Headers $headers
Write-Host "LIST OK: records count=$($list.data.records.Count), total=$($list.data.total)"

Write-Host "`n=== 6. Get Project ==="
$get = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$projectId" -Method Get -Headers $headers
Write-Host "GET OK: name=$($get.data.name), version=$($get.data.version)"

Write-Host "`n=== 7. Save Snapshot ==="
$snapBody = '{"topologyData":{"nodes":[],"segments":[],"lanes":[],"laneArrows":{},"halfEdges":[],"version":1},"ruleData":{"ruleSets":[],"odConfig":{"pairs":[]}},"description":"smoke test save"}'
$save = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$projectId/snapshot" -Method Put -ContentType 'application/json' -Headers $headers -Body $snapBody
Write-Host "SAVE OK: version=$($save.data.version)"

Write-Host "`n=== 8. List Templates ==="
$tpl = Invoke-RestMethod -Uri 'http://localhost:8080/api/templates' -Method Get -Headers $headers
Write-Host "TEMPLATES OK: count=$($tpl.data.Count)"

Write-Host "`n=== 9. List Reports ==="
$rpt = Invoke-RestMethod -Uri "http://localhost:8080/api/reports?projectId=$projectId" -Method Get -Headers $headers
Write-Host "REPORTS OK: records=$($rpt.data.records.Count), total=$($rpt.data.total)"

Write-Host "`n=== 10. Delete Project ==="
$del = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/$projectId" -Method Delete -Headers $headers
Write-Host "DELETE OK"

Write-Host "`n=== ALL SMOKE TESTS PASSED ==="
