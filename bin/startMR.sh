java -cp .\mymr-1.0.jar com.ksc.urltopn.driver.WordCountDriver
slave_config="slave.conf"
if [ -f "$slave_config" ]; then
    tail -n +2 "$slave_config" | while read -r ip akka_port netty_port memory cpu; do
	echo $ip $akka_port $netty_port $memory $cpu
        # 设置免密连接配置
        # ssh-copy-id "$ip"

        # 在远程机器上启动进程
        # ssh "$ip" "nohup java -jar your-jar-file.jar --akka-port $akka_port --netty-port $netty_port --memory $memory --cpu $cpu &"
    done
else
    echo "配置文件 $config_file 不存在"
    exit 1
fi
