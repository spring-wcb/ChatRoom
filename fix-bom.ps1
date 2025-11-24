# SpringChat BOM 清理脚本
# 用途：移除所有 Java 源文件中的 UTF-8 BOM，防止编译错误

Write-Host "=== SpringChat BOM 清理工具 ===" -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0
$checkedCount = 0

# 递归查找所有 Java 文件
Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object {
    $file = $_.FullName
    $checkedCount++
    
    # 读取文件字节
    $bytes = [System.IO.File]::ReadAllBytes($file)
    
    # 检查是否有 BOM (EF BB BF)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Host "[FIX] $($_.Name)" -ForegroundColor Yellow
        
        # 移除 BOM 并以 UTF-8 without BOM 重写
        $content = [System.Text.Encoding]::UTF8.GetString($bytes, 3, $bytes.Length - 3)
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file, $content, $utf8NoBom)
        
        $fixedCount++
    }
}

Write-Host ""
Write-Host "检查完成: $checkedCount 个文件" -ForegroundColor Green
if ($fixedCount -gt 0) {
    Write-Host "已修复: $fixedCount 个文件" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "请在 IntelliJ IDEA 中执行: Build -> Rebuild Project" -ForegroundColor Cyan
} else {
    Write-Host "没有发现 BOM，所有文件正常！" -ForegroundColor Green
}

