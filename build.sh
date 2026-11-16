#!/bin/bash
# ============================================================
# MindFlow 考研备考复习系统 - 构建 & 测试脚本
# 用法:
#   ./build.sh          # 编译源码 + 运行测试
#   ./build.sh compile  # 仅编译源码
#   ./build.sh test     # 编译并运行单元测试
#   ./build.sh run      # 编译并启动程序
#   ./build.sh clean    # 清理编译产物
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
TEST_DIR="$PROJECT_DIR/test"
LIB_DIR="$PROJECT_DIR/lib"
OUT_DIR="$PROJECT_DIR/out"
OUT_CLASSES="$OUT_DIR/classes"
OUT_TEST="$OUT_DIR/test"

JAVA_VERSION="21"

# ---------- 颜色输出 ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ---------- 检查 Java 版本 ----------
check_java() {
    if ! command -v javac &> /dev/null; then
        error "未找到 javac，请安装 JDK $JAVA_VERSION+"
    fi
    local version
    version=$(javac -version 2>&1 | grep -oE '[0-9]+' | head -1)
    if [ "$version" -lt "$JAVA_VERSION" ]; then
        error "需要 JDK $JAVA_VERSION+，当前版本: $(javac -version 2>&1)"
    fi
    info "Java 版本检查通过: $(javac -version 2>&1)"
}

# ---------- 编译源码 ----------
compile() {
    info "编译源码..."
    mkdir -p "$OUT_CLASSES"
    
    # 复制资源文件
    if [ -d "$SRC_DIR/resources" ]; then
        cp -r "$SRC_DIR/resources" "$OUT_CLASSES/"
    fi
    
    javac -d "$OUT_CLASSES" \
        -cp "$LIB_DIR/*" \
        --release "$JAVA_VERSION" \
        $(find "$SRC_DIR" -name '*.java')
    
    info "编译完成 → $OUT_CLASSES"
}

# ---------- 编译并运行测试 ----------
test() {
    compile
    
    if [ ! -d "$TEST_DIR" ] || [ -z "$(find "$TEST_DIR" -name '*.java' 2>/dev/null)" ]; then
        warn "未找到测试文件，跳过测试"
        return
    fi
    
    local junit_jar="$LIB_DIR/junit-platform-console-standalone-1.10.3.jar"
    if [ ! -f "$junit_jar" ]; then
        error "未找到 JUnit jar: $junit_jar"
    fi
    
    info "编译测试..."
    mkdir -p "$OUT_TEST"
    javac -d "$OUT_TEST" \
        -cp "$OUT_CLASSES:$LIB_DIR/*" \
        --release "$JAVA_VERSION" \
        $(find "$TEST_DIR" -name '*.java')
    
    info "运行单元测试..."
    java -jar "$junit_jar" \
        execute \
        --class-path "$OUT_TEST:$OUT_CLASSES:$LIB_DIR/*" \
        --scan-class-path "$OUT_TEST" \
        --details summary
}

# ---------- 运行程序 ----------
run() {
    compile
    info "启动 MindFlow..."
    java -cp "$OUT_CLASSES:$LIB_DIR/*" Main
}

# ---------- 清理 ----------
clean() {
    info "清理编译产物..."
    rm -rf "$OUT_DIR/classes" "$OUT_DIR/test"
    info "清理完成"
}

# ---------- 主入口 ----------
check_java

case "${1:-all}" in
    compile)  compile ;;
    test)     test ;;
    run)      run ;;
    clean)    clean ;;
    all)      test ;;
    *)
        echo "用法: $0 {compile|test|run|clean}"
        exit 1
        ;;
esac
