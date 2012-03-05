package perf;


import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Bela Ban
 */
public class Test {
    private Client[] clients;
    private CyclicBarrier barrier;

    private volatile
    static NumberFormat f;

    static {
        f=NumberFormat.getNumberInstance();
        f.setGroupingUsed(false);
        f.setMaximumFractionDigits(2);
    }



    private void start(String host, boolean silent, String setup_url, String read_url, String write_url, String destroy_url,
                       int num_threads, int num_requests, int num_attrs, int size, int write_percentage)
            throws NamingException, BrokenBarrierException, InterruptedException, MalformedURLException {
        this.clients=new Client[num_threads];
        this.barrier=new CyclicBarrier(num_threads + 1);

        System.out.println("Starting " + num_threads + " clients");
        for(int i=0; i < clients.length; i++) {
            Client client=new Client(barrier, host, silent, setup_url, read_url, write_url, destroy_url, write_percentage,
                                     num_requests, num_attrs, size);
            clients[i]=client;
            client.start();
        }

        System.out.println("Waiting for clients to initialize");
        barrier.await();
        
        long overallStart = System.currentTimeMillis();
        
        System.out.println("Waiting for clients to complete");
        barrier.await();
        
        long overallFinish = System.currentTimeMillis();
        
        long total_time=0, total_bytes_read=0, total_bytes_written=0;
        int total_successful_reads=0, total_successful_writes=0, total_failed_reads=0, total_failed_writes=0;

        int num_clients=0;
        for(Client client: clients) {
            if(!client.isSuccessful()) {
                continue;
            }
            num_clients++;
            total_time+=client.getTime();
            total_bytes_read+=client.getBytesRead();
            total_bytes_written+=client.getBytesWritten();
            total_successful_reads+=client.getSuccessfulReads();
            total_successful_writes+=client.getSuccessfulWrites();
            total_failed_reads+=client.getFailedReads();
            total_failed_writes+=client.getFailedWrites();
        }

        if(num_clients < 1) {
            System.err.println("No client completed successfully, will not compute results");
            return;
        }

        int failed_clients=num_threads - num_clients;
        int total_requests=total_successful_reads + total_successful_writes;
        double avg_time=total_time / num_clients;
        double reqs_sec=total_requests / (avg_time / 1000.0);
        long overall_time = overallStart - overallFinish;
//        double overall_avg_time = overall_time / num_clients;
//        double overall_reqs_sec = total_requests / (overall_avg_time / 1000.0);

        System.out.println("\nTotal requests: " + total_requests + " in (avg) " + (avg_time / 1000.0) + " secs");
        System.out.println("\n*** " + f.format(reqs_sec) + " requests/sec, requests/sec/client: " +
                f.format((total_requests / num_clients) / (avg_time / 1000.0)) + " ***\n");
//        System.out.println("\n*** " + f.format(overall_reqs_sec) + " requests/sec, requests/sec/client: " +
//              f.format((total_requests / num_clients) / (overall_avg_time / 1000.0)) + " ***\n");

        System.out.println("Successful reads: " + total_successful_reads + ", successful writes: " + total_successful_writes);
        System.out.println("Failed reads: " + total_failed_reads + ", failed writes: " + total_failed_writes);
        System.out.println("Bytes read: " + printBytes(total_bytes_read) + ", bytes written: " + printBytes(total_bytes_written));
        System.out.println("Bytes read/sec: " + printBytes(total_bytes_read / (avg_time / 1000.0)) + ", bytes written/sec: " +
                printBytes(total_bytes_written / (avg_time / 1000.0)));
        System.out.println("Total client: " + num_clients + ", failed clients: " + failed_clients);
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



    public static void main(String[] args) throws Exception {
        int num_threads=1;
        int num_requests=1000;
        int num_attrs=25;
        int size=1000;
        int write_percentage=10; // percent
        boolean silent=false;
        String host="localhost";
        String setup_url="web/setup.jsp";
        String read_url="web/read.jsp";
        String write_url="web/write.jsp";
        String destroy_url="web/destroy.jsp";

        for(int i=0; i < args.length; i++) {
            if(args[i].equals("-host")) {
                host=args[++i];
                continue;
            }
            if(args[i].equals("-silent")) {
                silent=true;
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
            if(args[i].equals("-write_percentage")) {
                write_percentage=Integer.parseInt(args[++i]);
                if(write_percentage < 0 || write_percentage > 100) {
                    System.err.println("write_percentage (" + write_percentage + ") has to be >= 0 && <= 100");
                    return;
                }
                continue;
            }
            help();
            return;
        }

        new Test().start(host, silent, setup_url, read_url, write_url, destroy_url, num_threads, num_requests, num_attrs, size, write_percentage);
    }

    private static void help() {
        System.out.println("Test [-host <host[:port] of apache>] [-silent] [-read_url <URL>] " +
                "[-num_threads <number of client sessions>] " +
                "[-write_url <URL>] [-setup_url <URL>] [-destroy_url <URL>] [-num_requests <requests>] " +
                "[-num_attrs <attrs>] [-size <bytes>] [-write_percentage <percentage, 0-100>]");
    }


    private static class Client extends Thread {
        private final int           read_percentage;
        private final int           num_requests, num_attrs, size;
        private final URL           setup_url, read_url, write_url, destroy_url;
        private final CyclicBarrier barrier;
        private int                 successful_reads=0, failed_reads=0, successful_writes=0, failed_writes=0;
        private long                bytes_read=0, bytes_written=0;
        private long                start=0, stop=0;
        private boolean             successful=true;
        private final byte[]        buffer=new byte[1024];
        private String              cookie=null;
        private boolean             silent=false;




        private Client(CyclicBarrier barrier, String host, boolean silent,
                       String setup_url, String read_url, String write_url, String destroy_url,
                       int write_percentage, int num_requests, int num_attrs, int size) throws MalformedURLException {
            this.barrier=barrier;
            this.silent=silent;
            this.read_percentage=100 - write_percentage;
            this.num_requests=num_requests;
            this.num_attrs=num_attrs;
            this.size=size;
            String tmp="http://" + host + "/";
            this.setup_url=new URL(tmp + setup_url + "?num_attrs=" + num_attrs + "&size=" + size);
            this.read_url=new URL(tmp + read_url + "?id=");
            this.write_url=new URL(tmp + write_url + "?size=" + size + "&id=");
            this.destroy_url=new URL(tmp + destroy_url);
        }

        public void run() {
            boolean inited = false;
            try {
                init();
                log("inited: " + barrier.getNumberWaiting() + " threads waiting");
                try { barrier.await(); } finally { inited = true; }
                start=System.currentTimeMillis();
                loop(num_requests);
            }
            catch(Exception e) {
                error("failure", e);
                successful=false;
                if (!inited)
                   try { barrier.await(); }  catch(Exception e1) {}
            }
            finally {
                stop=System.currentTimeMillis();
                try {terminate();} catch(IOException e) {}
                log("completed: " + barrier.getNumberWaiting() + " threads waiting");
                try {barrier.await();} catch(Exception e) {}
            }
        }



        public long getBytesRead() {
            return bytes_read;
        }

        public long getBytesWritten() {
            return bytes_written;
        }

        public int getFailedReads() {
            return failed_reads;
        }

        public int getFailedWrites() {
            return failed_writes;
        }

        public int getSuccessfulReads() {
            return successful_reads;
        }

        public int getSuccessfulWrites() {
            return successful_writes;
        }
        
        public long getTime() {
            return stop - start;
        }

        public boolean isSuccessful() {
            return successful;
        }

        /** Create NUM_SESSIONS sessions with NUM_ATTRS attributes of SIZE size. Total size is multiplication of the 3 */
        private void init() throws IOException {
            executeRequest(setup_url);
        }

        private void terminate() throws IOException {
            executeRequest(destroy_url);
            if(!silent)
                log("destroyed session");
        }

        private void loop(int num_requests) throws IOException {
            int random, id;
            int print=num_requests / 10;
            int rc, total=0;

            for(int i=0; i < num_requests; i++) {
                random=(int)random(100);
                id=(int)random(num_attrs -1);
                if(random <= read_percentage) { // read
                    URL tmp=new URL(read_url + String.valueOf(id));
                    rc=executeRequest(tmp);
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
                    rc=executeRequest(tmp);
                    if(rc == 200) {
                        successful_writes++;
                        bytes_written+=size; // bytes read from the session, not by the HttpClient !
                    }
                    else {
                        failed_writes++;
                    }                    
                }
                total++;
                if(!silent && print > 0 && total % print == 0)
                    log(total + " / " + num_requests);
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
                if(tmp_cookie != null) {
                    if (cookie == null) {
                        cookie=tmp_cookie;
                        if(!silent)
                            System.out.println("set-cookie: " + cookie);
                    }
                    else if (!silent) {
                        System.out.println("Cookie changed: was: " + cookie + " is: " + tmp_cookie);
                    }
                }
                return conn.getResponseCode();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }



        private static void log(String msg) {
            System.out.println("[thread-" + Thread.currentThread().getId() + "]: " + msg);
        }

        private static void error(String msg, Throwable th) {
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

        private static long random(long range) {
            return (long)((Math.random() * 100000) % range) + 1;
        }
    }



//    private static class MyCookieHandler extends CookieHandler {
//        Map<URI,List<String>> cookies=new HashMap<URI,List<String>>();
//
//        public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
//            Map<String,List<String>> map=new HashMap<String,List<String>>();
//            List<String> list=cookies.get(uri);
//            if(list == null) {
//                list=new LinkedList<String>();
//                cookies.put(uri, list);
//            }
//            map.put("cookie", list);
//            return Collections.unmodifiableMap(map);
//        }
//
//        public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
//            List<String> list=responseHeaders.get("Set-Cookie");
//            if(list != null) {
//                cookies.get(uri).addAll(list);
//            }
//        }
//    }


}
