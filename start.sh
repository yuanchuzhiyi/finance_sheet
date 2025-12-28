#!/bin/bash

# 财务报表项目一键启动脚本
# 同时启动前端 (Vite) 和后端 (FastAPI) 服务

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 清理函数 - 用于退出时关闭所有后台进程
cleanup() {
    print_info "正在关闭所有服务..."
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null
        print_info "前端服务已关闭"
    fi
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null
        print_info "后端服务已关闭"
    fi
    print_success "所有服务已关闭，再见！"
    exit 0
}

# 捕获退出信号
trap cleanup SIGINT SIGTERM

echo ""
echo "=================================================="
echo "        📊 财务报表项目 - 一键启动脚本"
echo "=================================================="
echo ""

# 检查 node 是否安装
if ! command -v node &> /dev/null; then
    print_error "未检测到 Node.js，请先安装 Node.js"
    exit 1
fi
print_success "Node.js 已安装: $(node --version)"

# 检查 npm 是否安装
if ! command -v npm &> /dev/null; then
    print_error "未检测到 npm，请先安装 npm"
    exit 1
fi
print_success "npm 已安装: $(npm --version)"

# 检查 Python 是否安装
if ! command -v python3 &> /dev/null; then
    print_error "未检测到 Python3，请先安装 Python3"
    exit 1
fi
print_success "Python3 已安装: $(python3 --version)"

# 检查 .env 文件是否存在
if [ ! -f ".env" ]; then
    print_warning ".env 文件不存在，正在从 .env.example 复制..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        print_warning "请编辑 .env 文件并填入正确的 API Key"
    else
        print_warning "未找到 .env.example 文件"
    fi
fi

# 检查并安装前端依赖
if [ ! -d "node_modules" ]; then
    print_info "正在安装前端依赖..."
    npm install
    if [ $? -ne 0 ]; then
        print_error "前端依赖安装失败"
        exit 1
    fi
    print_success "前端依赖安装完成"
else
    print_success "前端依赖已存在"
fi

# 检查并创建 Python 虚拟环境
if [ ! -d "server/.venv" ]; then
    print_info "正在创建 Python 虚拟环境..."
    python3 -m venv server/.venv
    if [ $? -ne 0 ]; then
        print_error "Python 虚拟环境创建失败"
        exit 1
    fi
    print_success "Python 虚拟环境创建完成"
fi

# 激活虚拟环境并安装后端依赖
print_info "正在检查后端依赖..."
source server/.venv/bin/activate
pip install -r server/requirements.txt -q
if [ $? -ne 0 ]; then
    print_error "后端依赖安装失败"
    exit 1
fi
print_success "后端依赖已就绪"

echo ""
echo "=================================================="
echo "              🚀 启动服务"
echo "=================================================="
echo ""

# 启动后端服务
print_info "正在启动后端服务 (FastAPI on port 8000)..."
cd server
.venv/bin/uvicorn main:app --reload --port 8000 &
BACKEND_PID=$!
cd ..

# 等待后端启动
sleep 2

# 检查后端是否启动成功
if ps -p $BACKEND_PID > /dev/null; then
    print_success "后端服务已启动 (PID: $BACKEND_PID)"
else
    print_error "后端服务启动失败"
    exit 1
fi

# 启动前端服务
print_info "正在启动前端服务 (Vite on port 5173)..."
npm run dev &
FRONTEND_PID=$!

# 等待前端启动
sleep 3

# 检查前端是否启动成功
if ps -p $FRONTEND_PID > /dev/null; then
    print_success "前端服务已启动 (PID: $FRONTEND_PID)"
else
    print_error "前端服务启动失败"
    cleanup
    exit 1
fi

echo ""
echo "=================================================="
echo "              ✅ 所有服务已启动"
echo "=================================================="
echo ""
print_success "前端地址: 请查看上方 Vite 输出的 Local 地址"
print_success "后端地址: http://localhost:8000"
print_success "API 文档: http://localhost:8000/docs"
echo ""
print_info "按 Ctrl+C 停止所有服务"
echo ""

# 等待用户中断
wait
