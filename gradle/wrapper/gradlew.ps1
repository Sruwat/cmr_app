param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Arguments
)

$ErrorActionPreference = 'Stop'
$appHome = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path))

$propsFile = Join-Path $appHome 'gradle/wrapper/gradle-wrapper.properties'
$distUrl = (Select-String -Path $propsFile -Pattern '^distributionUrl=').Line.Split('=', 2)[1]
$distUrl = $distUrl -replace '\\:', ':'
$distFile = Split-Path $distUrl -Leaf
$gradleVersion = [regex]::Match($distFile, '^gradle-(.+)-bin\.zip$').Groups[1].Value
$gradleUserHome = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $env:USERPROFILE '.gradle' }
$cacheDir = Join-Path $gradleUserHome "wrapper/dists/gradle-$gradleVersion-bin"
$zipPath = Join-Path $cacheDir $distFile
$installDir = Join-Path $cacheDir "gradle-$gradleVersion"

New-Item -ItemType Directory -Force -Path $cacheDir | Out-Null

if (-not (Test-Path (Join-Path $installDir 'bin\gradle.bat'))) {
    if (-not (Test-Path $zipPath)) {
        Invoke-WebRequest -Uri $distUrl -OutFile $zipPath
    }
    if (Test-Path $installDir) {
        Remove-Item -Recurse -Force $installDir
    }
    Expand-Archive -Path $zipPath -DestinationPath $cacheDir -Force
}

& (Join-Path $installDir 'bin\gradle.bat') -p $appHome @Arguments
exit $LASTEXITCODE
