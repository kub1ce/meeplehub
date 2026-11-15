$ErrorActionPreference = 'Stop'
$version = '3.10.0'
$distributionName = "apache-maven-$version"
$installRoot = Join-Path $env:USERPROFILE ".m2\wrapper\dists\meeplehub-$distributionName"
$mavenHome = Join-Path $installRoot $distributionName
$mavenCommand = Join-Path $mavenHome 'bin\mvn.cmd'
$archive = Join-Path $env:TEMP "$distributionName-bin.zip"
$downloadUrl = "https://dlcdn.apache.org/maven/maven-3/$version/binaries/$distributionName-bin.zip"
$expectedSha256 = '1f6d9909266510f039f59aa0e13dcd2c66da85f043e56276e41f2918f8bddaff'

if (-not (Test-Path $mavenCommand)) {
    Write-Host "Downloading Apache Maven $version..."
    New-Item -ItemType Directory -Force -Path $installRoot | Out-Null
    Invoke-WebRequest -Uri $downloadUrl -OutFile $archive

    $actualSha256 = (Get-FileHash -Path $archive -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualSha256 -ne $expectedSha256) {
        Remove-Item -Force $archive -ErrorAction SilentlyContinue
        throw 'Maven distribution checksum mismatch. The archive was not installed.'
    }

    Expand-Archive -LiteralPath $archive -DestinationPath $installRoot -Force
    Remove-Item -Force $archive
}

& $mavenCommand @args
exit $LASTEXITCODE
