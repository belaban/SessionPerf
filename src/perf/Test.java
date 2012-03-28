package perf;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests web session clustering performance
 * @author Bela Ban
 */
public class Test {
    private final Client[]      clients;
    private final CyclicBarrier barrier;
    private final int           num_requests;
    private final int           num_attrs;
    private final int           size;
    private final int           num_threads;
    private final int           read_percentage;
    private final URL           setup_url;
    private final URL           read_url;
    private final URL           write_url;
    private final URL           destroy_url;
    private final AtomicInteger curr_num_reqs=new AtomicInteger(0);
    private final int           print;

    private volatile static NumberFormat f=NumberFormat.getNumberInstance();

    static {
        f.setGroupingUsed(false);
        f.setMaximumFractionDigits(2);
    }



    public Test(String host, int num_requests, int num_attrs, int size, int num_threads, int read_percentage,
                String setup_url, String read_url, String write_url, String destroy_url) throws Exception {
        this.num_attrs=num_attrs;
        this.size=size;
        this.num_threads=num_threads;
        this.read_percentage=read_percentage;
        this.num_requests=num_requests;
        this.print=num_requests / 10;

        String tmp="http://" + host + "/";
        this.setup_url=new URL(tmp + setup_url + "?num_attrs=" + num_attrs + "&size=" + size);
        this.read_url=new URL(tmp + read_url + "?id=");
        this.write_url=new URL(tmp + write_url + "?size=" + size + "&id=");
        this.destroy_url=new URL(tmp + destroy_url);

        this.clients=new Client[num_threads];
        this.barrier=new CyclicBarrier(num_threads + 1);
    }





    private void start() throws Exception {
        System.out.println("\nStarting " + num_threads + " clients for a total of " + num_requests + " requests");
        for(int i=0; i < clients.length; i++) {
            clients[i]=new Client();
            clients[i].start();
        }

        barrier.await();
        long start=System.currentTimeMillis();

        barrier.await();
        long total_time=System.currentTimeMillis() - start;
        
        long total_bytes_read=0, total_bytes_written=0;
        int  total_successful_reads=0, total_successful_writes=0, total_failed_reads=0, total_failed_writes=0;

        int num_clients=0;
        for(Client client: clients) {
            if(!client.successful)
                continue;
            num_clients++;
            total_bytes_read+=client.bytes_read;
            total_bytes_written+=client.bytes_written;
            total_successful_reads+=client.successful_reads;
            total_successful_writes+=client.successful_writes;
            total_failed_reads+=client.failed_reads;
            total_failed_writes+=client.failed_writes;
        }

        if(num_clients < 1) {
            System.err.println("No client completed successfully, will not compute results");
            return;
        }

        int failed_clients=num_threads - num_clients;
        int total_requests=total_successful_reads + total_successful_writes;
        double reqs_sec=total_requests / (total_time / 1000.0);
        double reqs_sec_client=reqs_sec / num_clients;

        System.out.println("\nTotal requests: " + total_requests + " in " + (total_time / 1000.0) + " secs");
        System.out.println("\n\033[1m" + f.format(reqs_sec) + " requests/sec, requests/sec/client: " +
                             f.format(reqs_sec_client) + "\033[0m\n");

        System.out.println("Successful reads: " + total_successful_reads + ", successful writes: " + total_successful_writes
                             + " (failed reads: " + total_failed_reads + ", failed writes: " + total_failed_writes + ")");
        System.out.println("Read: " + printBytes(total_bytes_read) + ", written: " + printBytes(total_bytes_written));
        System.out.println("Successful clients: " + num_clients + ", failed clients: " + failed_clients + "\n");
    }
    

    private static String printBytes(long bytes) {
        double tmp;

        if(bytes < 1000)
            return bytes + "b";
        if(bytes < 1000000) {
            tmp=bytes / 1000.0;
            return f.format(tmp) + "KB";
        }
        if(bytes < 1000000000) {
            tmp=bytes / 1000000.0;
            return f.format(tmp) + "MB";
        }
        else {
            tmp=bytes / 1000000000.0;
            return f.format(tmp) + "GB";
        }
    }

    public static String printBytes(double bytes) {
        double tmp;

        if(bytes < 1000)
            return bytes + "b";
        if(bytes < 1000000) {
            tmp=bytes / 1000.0;
            return f.format(tmp) + "KB";
        }
        if(bytes < 1000000000) {
            tmp=bytes / 1000000.0;
            return f.format(tmp) + "MB";
        }
        else {
            tmp=bytes / 1000000000.0;
            return f.format(tmp) + "GB";
        }
    }


     private static long random(long range) {
         return (long)((Math.random() * 100000) % range) + 1;
     }
    

    public static void main(String[] args) throws Exception {
        int num_threads=50;
        int num_requests=100000;
        int num_attrs=10;
        int size=1000;
        int read_percentage=90; // percent
        String host="localhost:8000";
        String setup_url="web/setup.jsp";
        String read_url="web/read.jsp";
        String write_url="web/write.jsp";
        String destroy_url="web/destroy.jsp";

        for(int i=0; i < args.length; i++) {
            if(args[i].equals("-host")) {
                host=args[++i];
                continue;
            }
            if(args[i].equals("-setup_url")) {
                setup_url=args[++i];
                continue;
            }
            if(args[i].equals("-read_url")) {
                read_url=args[++i];
                continue;
            }
            if(args[i].equals("-write_url")) {
                write_url=args[++i];
                continue;
            }
            if(args[i].equals("-destroy_url")) {
                destroy_url=args[++i];
                continue;
            }
            if(args[i].equals("-num_threads")) {
                num_threads=Integer.parseInt(args[++i]);
                continue;
            }
            if(args[i].equals("-num_requests")) {
                num_requests=Integer.parseInt(args[++i]);
                continue;
            }
            if(args[i].equals("-num_attrs")) {
                num_attrs=Integer.parseInt(args[++i]);
                continue;
            }
            if(args[i].equals("-size")) {
                size=Integer.parseInt(args[++i]);
                continue;
            }
            if(args[i].equals("-read_percentage")) {
                read_percentage=Integer.parseInt(args[++i]);
                if(read_percentage < 0 || read_percentage > 100) {
                    System.err.println("read_percentage (" + read_percentage + ") has to be >= 0 && <= 100");
                    return;
                }
                continue;
            }
            help();
            return;
        }

        Test test=new Test(host, num_requests, num_attrs, size, num_threads, read_percentage,
                           setup_url, read_url, write_url, destroy_url);
        test.start();
    }

    private static void help() {
        System.out.println("Test [-host <host[:port] of apache>] [-read_url <URL>] " +
                "[-num_threads <number of client sessions>] " +
                "[-write_url <URL>] [-setup_url <URL>] [-destroy_url <URL>] [-num_requests <requests>] " +
                "[-num_attrs <attrs>] [-size <bytes>] [-read_percentage <percentage, 0-100>]");
    }


    private class Client extends Thread {
        private int                 successful_reads=0, failed_reads=0, successful_writes=0, failed_writes=0;
        private long                bytes_read=0, bytes_written=0;
        private boolean             successful=true;
        private final byte[]        buffer=new byte[1024];
        private String              cookie=null;



        public void run() {
            boolean initialized = false;
            try {
                init();
                try { barrier.await(); } finally { initialized = true; }
                loop();
            }
            catch(Exception e) {
                error("failure", e);
                successful=false;
                if (!initialized)
                   try { barrier.await(); }  catch(Exception e1) {}
            }
            finally {
                try {terminate();} catch(IOException e) {}
                try {barrier.await();} catch(Exception e) {}
            }
        }


        /** Create NUM_SESSIONS sessions with NUM_ATTRS attributes of SIZE size. Total size is multiplication of the 3 */
        private void init() throws IOException {
            executeRequest(setup_url);
        }

        private void terminate() throws IOException {
            executeRequest(destroy_url);
        }

        private void loop() throws IOException {
            for(;;) {
                int req=curr_num_reqs.incrementAndGet();
                if(req > num_requests)
                    break;

                int random=(int)random(100);
                int id=(int)random(num_attrs -1);
                if(random <= read_percentage) { // read
                    URL tmp=new URL(read_url + String.valueOf(id));
                    int rc=executeRequest(tmp);
                    if(rc == 200) {
                        successful_reads++;
                        bytes_read+=size; // bytes read from the session, not by the HttpClient !
                    }
                    else {
                        failed_reads++;
                    }
                }
                else {             // write
                    URL tmp=new URL(write_url + String.valueOf(id));
                    int rc=executeRequest(tmp);
                    if(rc == 200) {
                        successful_writes++;
                        bytes_written+=size; // bytes read from the session, not by the HttpClient !
                    }
                    else {
                        failed_writes++;
                    }                    
                }
                if(print > 0 && req % print == 0)
                    log(req + " / " + num_requests);
            }
        }



        private int executeRequest(URL url) throws IOException {
            InputStream input=null;
            HttpURLConnection conn=null;
            try {
                conn=(HttpURLConnection)url.openConnection(); // not yet connected
                if(cookie != null)
                    conn.setRequestProperty("Cookie", cookie);
                
                input=conn.getInputStream(); // NOW it is connected
                while(input.read(buffer) > 0) {
                    ;
                }
                input.close(); // discard data
                String tmp_cookie=conn.getHeaderField("set-cookie");
                if(tmp_cookie != null && cookie == null)
                    cookie=tmp_cookie;
                return conn.getResponseCode();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }



        private void log(String msg) {
            System.out.println("[thread-" + Thread.currentThread().getId() + "]: " + msg);
        }

        private void error(String msg, Throwable th) {
            String tmp="[thread-" + Thread.currentThread().getId() + "]: " + msg;
            if(th != null) {
                tmp+=", ex: " + th + "\n";
                StringWriter writer = new StringWriter();
                PrintWriter pw = new PrintWriter(writer);
                th.printStackTrace(pw);
                pw.flush();
                tmp+= writer.toString();
            }
            System.err.println(tmp);
        }

    }



}
