# Запуск unit/integration тестов Terra Fabrica в офлайн-окружении.
#
# Почему не `gradle test`: сеть закрыта, а кэш Gradle содержит jar-ы JUnit без
# модульных метаданных, поэтому разрешение зависимостей падает с
# "No cached version ... available for offline mode". Скрипт берёт уже скачанные
# артефакты из %USERPROFILE%\.gradle\caches\modules-2 и запускает JUnit Platform
# Launcher напрямую. В обычном (сетевом) окружении достаточно `gradle test`.
#
# Требует предварительной компиляции основного кода: gradle compileJava processResources.

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$cache = Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1'

function Find-Jar([string]$pattern) {
    $hit = Get-ChildItem $cache -Recurse -File -Filter $pattern -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } |
        Select-Object -First 1
    if (-not $hit) { throw "не найден артефакт $pattern в кэше Gradle" }
    return $hit.FullName
}

$jars = @(
    (Find-Jar 'junit-jupiter-api-5.11.4.jar'),
    (Find-Jar 'junit-jupiter-engine-5.11.4.jar'),
    (Find-Jar 'junit-platform-commons-1.11.4.jar'),
    (Find-Jar 'junit-platform-engine-1.11.4.jar'),
    (Find-Jar 'junit-platform-launcher-1.11.4.jar'),
    (Find-Jar 'opentest4j-*.jar'),
    (Find-Jar 'apiguardian-api-*.jar'),
    (Find-Jar 'gson-2.10.1.jar')
)

$mainClasses = Join-Path $root 'build\classes\java\main'
$mainResources = Join-Path $root 'build\resources\main'
foreach ($dir in @($mainClasses, $mainResources)) {
    if (-not (Test-Path $dir)) {
        throw "нет $dir — сначала выполните: gradle --offline compileJava processResources"
    }
}

$testOut = Join-Path $root 'build\classes\java\test-offline'
if (Test-Path $testOut) { Remove-Item $testOut -Recurse -Force }
New-Item -ItemType Directory -Path $testOut -Force | Out-Null

$classpath = (($jars + $mainClasses + $mainResources) -join ';')
$testSources = Get-ChildItem (Join-Path $root 'src\test\java') -Recurse -File -Filter '*.java' |
    ForEach-Object { $_.FullName }
$runner = Join-Path $root 'tools\java\TfTestRunner.java'

$javacArgs = @('-encoding', 'UTF-8', '-nowarn', '-d', $testOut, '-cp', $classpath) + $testSources + $runner
& javac @javacArgs
if ($LASTEXITCODE -ne 0) { throw "компиляция тестов не удалась (код $LASTEXITCODE)" }

& java '-Dfile.encoding=UTF-8' -cp "$testOut;$classpath" TfTestRunner com.terrafabrica
exit $LASTEXITCODE
