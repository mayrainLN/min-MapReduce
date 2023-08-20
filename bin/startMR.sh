nohup java -cp mymr-1.0.jar com.ksc.urltopn.driver.UrlTopNDriver &

# 读取第二行内容并保存到变量
second_line=$(sed -n '2p' master.conf)

# 将连续的空格替换为单个空格，并使用空格分隔参数
params=($(echo $second_line | tr -s ' '))

# 保存每个参数到不同的变量
master_ip="${params[0]}"
master_akka_port="${params[1]}"
master_thrift_port="${params[2]}"
master_memory="${params[3]}"

# 打印变量的值
echo "master配置："
echo "IP: $master_ip"
echo "Akka端口: $master_akka_port"
echo "Thrift端口: $master_thrift_port"
echo "内存: $master_memory"

slave_config="slave.conf"
if [ -f "$slave_config" ]; then
    tail -n +2 "$slave_config" | while read -r ip akka_port netty_port memory cpu; do
      echo "slave配置:"
      echo $ip $akka_port $netty_port $memory $cpu
            # 设置免密连接配置
            # ssh-copy-id "$ip"

            # 在远程机器上启动进程
            # ssh "$ip" "nohup java -jar your-jar-file.jar --akka-port $akka_port --netty-port $netty_port --memory $memory --cpu $cpu &"
      nohup java -DmasterHost=$master_ip -DmasterPort=$master_akka_port -Dcore=$cpu -Dhost=$ip -Dport=$akka_port -Dmemory=$memory -DnettyPort=$netty_port -cp mymr-1.0.jar com.ksc.urltopn.worker.Executor &
    done
else
    echo "配置文件 $config_file 不存在"
    exit 1
fi