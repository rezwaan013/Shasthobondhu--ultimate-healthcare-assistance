param(
    [string]$ProjectId = "shasthobondhu-d5bdf",
    [string]$Namespace = "shasthobondhu-d5bdf-default-rtdb",
    [string]$HostName = "127.0.0.1",
    [int]$Port = 9000
)

$ErrorActionPreference = "Stop"
$base = "http://${HostName}:${Port}"

function New-EmulatorAccount([string]$Email) {
    $body = @{ email = $Email; password = "Password123"; returnSecureToken = $true } `
        | ConvertTo-Json -Compress
    return Invoke-RestMethod -Method Post -ContentType "application/json" `
        -Uri "http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake-api-key" `
        -Body $body
}

function Invoke-Db {
    param(
        [string]$Path,
        [string]$Method = "GET",
        [object]$Body = $null,
        [string]$Token = ""
    )
    $uri = "$base/$Path.json?ns=$Namespace"
    $headers = @{}
    if ($Token -eq "__ADMIN__") {
        $headers.Authorization = "Bearer owner"
    } elseif ($Token) {
        $uri += "&auth=$([Uri]::EscapeDataString($Token))"
    }
    $json = if ($null -eq $Body) { $null } else { $Body | ConvertTo-Json -Depth 12 -Compress }
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $uri -Method $Method `
            -Headers $headers -ContentType "application/json" -Body $json
        return @{ Status = [int]$response.StatusCode; Content = $response.Content }
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        return @{ Status = $status; Content = $_.Exception.Message }
    }
}

function Assert-Allowed($Result, [string]$Label) {
    if ($Result.Status -lt 200 -or $Result.Status -ge 300) {
        throw "$Label expected ALLOW, got HTTP $($Result.Status): $($Result.Content)"
    }
    Write-Output "PASS allow: $Label"
}

function Assert-Denied($Result, [string]$Label) {
    if ($Result.Status -ge 200 -and $Result.Status -lt 300) {
        throw "$Label expected DENY, got HTTP $($Result.Status)"
    }
    Write-Output "PASS deny:  $Label"
}

$aliceAccount = New-EmulatorAccount "alice@example.com"
$bobAccount = New-EmulatorAccount "bob@example.com"
$alice = $aliceAccount.idToken
$bob = $bobAccount.idToken
$aliceUid = $aliceAccount.localId
$bobUid = $bobAccount.localId
$adminBypass = "__ADMIN__"
$now = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

Assert-Denied (Invoke-Db "users/$aliceUid") "unauthenticated user-profile read"

$aliceProfile = @{
    uid = $aliceUid; name = "Alice Donor"; email = "alice@example.com"
    phone = "01700000000"; role = "USER"; updatedAt = $now
}
Assert-Allowed (Invoke-Db "users/$aliceUid" "PUT" $aliceProfile $alice) "own user-profile write"
Assert-Denied (Invoke-Db "users/$bobUid" "PUT" $aliceProfile $alice) "other user-profile write"
Assert-Denied (Invoke-Db "users/$aliceUid" "GET" $null $bob) "other private profile read"

$donor = @{
    uid = $aliceUid; name = "Alice Donor"; bloodGroup = "A+"; phone = "01700000000"
    division = "Dhaka"; district = "Dhaka"; upazila = "Mirpur"
    location = "Dhaka, Mirpur"; lastDonationDate = "Not shared"
    available = $true; availableAfterEpoch = 0; updatedAt = $now
}
Assert-Allowed (Invoke-Db "blood_donors/$aliceUid" "PUT" $donor $alice) "own donor write"
Assert-Denied (Invoke-Db "blood_donors/$bobUid" "PUT" $donor $alice) "other donor write"
Assert-Allowed (Invoke-Db "blood_donors" "GET" $null $bob) "authenticated donor discovery"

Assert-Allowed (Invoke-Db "user_status/$aliceUid" "PUT" `
    @{ online = $true; lastSeen = $now } $alice) "own presence write"
Assert-Denied (Invoke-Db "user_status/$bobUid" "PUT" `
    @{ online = $true; lastSeen = $now } $alice) "other presence write"

$request = @{
    requestId = "request-1"; requesterId = $aliceUid; patientName = "Patient One"
    patientDetails = "Emergency surgery"; bloodGroup = "A+"; units = 2
    hospital = "Dhaka Medical"; location = "Dhaka"; phone = "01700000000"
    urgency = "Urgent"; neededDate = "08/11/2026"; neededTime = "10:30"
    status = "Open"; timestamp = $now; updatedAt = $now
}
Assert-Allowed (Invoke-Db "blood_requests/request-1" "PUT" $request $alice) "owner request creation"
$tampered = $request.Clone(); $tampered.acceptedByUid = $bobUid
$tampered.acceptedByName = "Bob Donor"; $tampered.updatedAt = $now + 1
Assert-Denied (Invoke-Db "blood_requests/request-1" "PUT" $tampered $alice) "requester cannot forge acceptance"
$invalid = $request.Clone(); $invalid.status = "Fulfilled"; $invalid.updatedAt = $now + 1
Assert-Denied (Invoke-Db "blood_requests/request-1" "PUT" $invalid $alice) "invalid Open-to-Fulfilled transition"
$accepted = $request.Clone(); $accepted.status = "Accepted"; $accepted.acceptedByUid = $bobUid
$accepted.acceptedByName = "Bob Donor"; $accepted.updatedAt = $now + 2
Assert-Allowed (Invoke-Db "blood_requests/request-1" "PUT" $accepted $bob) "other donor accepts Open request"
Assert-Allowed (Invoke-Db "blood_request_status/request-1" "PUT" `
    @{ status = "Accepted"; updatedBy = $bobUid; timestamp = $now + 2 } $bob) "accepted donor status record"
$fulfilled = $accepted.Clone(); $fulfilled.status = "Fulfilled"; $fulfilled.updatedAt = $now + 3
Assert-Allowed (Invoke-Db "blood_requests/request-1" "PUT" $fulfilled $alice) "owner completes Accepted request"
Assert-Allowed (Invoke-Db "blood_request_status/request-1" "PUT" `
    @{ status = "Fulfilled"; updatedBy = $aliceUid; timestamp = $now + 3 } $alice) "owner completion status record"
Assert-Denied (Invoke-Db "blood_request_status/request-1" "PUT" `
    @{ status = "Fulfilled"; updatedBy = $bobUid; timestamp = $now + 4 } $bob) "accepted donor cannot overwrite owner completion status"
Assert-Denied (Invoke-Db "blood_requests/request-1" "PUT" $request $bob) "other donor cannot reopen request"

$notification = @{
    title = "Blood request accepted"; message = "A donor accepted your request"
    type = "REQUEST"; requestId = "request-1"; createdAt = $now; read = $false
}
Assert-Denied (Invoke-Db "notifications/$aliceUid/n1" "PUT" $notification $alice) "client notification creation"
Assert-Allowed (Invoke-Db "notifications/$aliceUid/n1" "PUT" $notification $adminBypass) "trusted Admin SDK notification seed"
$notification.read = $true
Assert-Allowed (Invoke-Db "notifications/$aliceUid/n1" "PUT" $notification $alice) "owner marks notification read"
Assert-Denied (Invoke-Db "notifications/$aliceUid" "GET" $null $bob) "other notification read"

$hospital = @{
    hospitalId = "hospital-1"; name = "Authorized Hospital"; address = "Dhaka"
    phone = "02123456789"; services = "Emergency, ICU"; location = "Dhaka"
    updatedAt = $now
}
Assert-Denied (Invoke-Db "hospitals/hospital-1" "PUT" $hospital $alice) "ordinary hospital write"
Assert-Allowed (Invoke-Db "hospitals/hospital-1" "PUT" $hospital $adminBypass) "trusted Admin SDK hospital seed"
Assert-Allowed (Invoke-Db "hospitals" "GET" $null $alice) "authenticated hospital read"

$medicine = @{
    medicineId = "medicine-1"; name = "Paracetamol"; brand = "Generic"
    pharmacy = "Authorized Pharmacy"; location = "Dhaka"; availability = "In Stock"
    price = 10; phone = "01700000001"; address = "Dhaka"; lastUpdated = "2026-11-08"
    updatedAt = $now
}
Assert-Denied (Invoke-Db "medicines/medicine-1" "PUT" $medicine $alice) "ordinary medicine write"
Assert-Allowed (Invoke-Db "medicines/medicine-1" "PUT" $medicine $adminBypass) "trusted Admin SDK medicine seed"
Assert-Allowed (Invoke-Db "medicines" "GET" $null $alice) "authenticated medicine read"

Write-Output "RTDB_RULES_SMOKE_TESTS_PASSED"
