/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.flink;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author minwoo.jung
 */
public class SocketServer {


    private static ConcurrentLinkedDeque queue   = getBigRawData();


    public static void main(String[] args) throws Exception {
            System.out.println("!stat");

        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(9600);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                    socket = serverSocket.accept();
            } catch (IOException e) {
                    System.out.println("I/O error: " + e);
            }
            // new threa for a client
            new EchoThread(socket, queue).start();
        }
    }

        public static class EchoThread extends Thread {
            protected Socket socket;
            private ConcurrentLinkedDeque queue;

            public EchoThread(Socket clientSocket, ConcurrentLinkedDeque queue) {
                    this.socket = clientSocket;
                    this.queue = queue;
            }

                public void run() {
                    InputStream inp = null;
                    BufferedReader brinp = null;
                    DataOutputStream out = null;
                    try {
//                                inp = socket.getInputStream();
//                                brinp = new BufferedReader(new InputStreamReader(inp));
                                out = new DataOutputStream(socket.getOutputStream());
                        } catch (IOException e) {
                                return;
                        }
                        String line;
                        while (true) {
                                try {
//                                        line = brinp.readLine();
//                                        if ((line == null) || line.equalsIgnoreCase("QUIT")) {
//                                                socket.close();
//                                                return;
//                                        } else {

                                        while(true) {
                                                String value = (String) queue.poll();

                                                if(value == null) {
                                                        break;
                                                }
                                                String v = value + "\n";
                                                System.out.println(v);
                                                out.writeBytes(v);
                                                out.flush();
                                        }
                                } catch (IOException e) {
                                        e.printStackTrace();
                                        return;
                                }
                        }
                }
        }

        public static ConcurrentLinkedDeque getBigRawData() {
                ConcurrentLinkedDeque queue = new ConcurrentLinkedDeque();

                for(Long i=0L; i < 10000000 ; i=i+5000L) {
                        queue.addAll(getRawData(i));
                }

                return queue;
        }

        public static List<String> getRawData(Long seed) {
                final String AGENT_1 = "agent1";
                final String AGENT_2 = "agent2";
                final String AGENT_3 = "agent3";
                final String AGENT_4 = "agent4";
                final String AGENT_5 = "agent5";

                List<String> rawData = new ArrayList<>();
                // window 1
                Long windows1Seed = 100L;
                windows1Seed = windows1Seed + seed;
                rawData.add(AGENT_1 + "," + windows1Seed + "," + 1);
                rawData.add(AGENT_1 + "," + windows1Seed + "," + 2);
                rawData.add(AGENT_1 + "," + windows1Seed + "," + 3);
                rawData.add(AGENT_1 + "," + windows1Seed + "," + 4);
                rawData.add(AGENT_1 + "," + windows1Seed + "," + 5);

                rawData.add(AGENT_2 + "," + windows1Seed + "," + 101);
                rawData.add(AGENT_2 + "," + windows1Seed + "," + 102);
                rawData.add(AGENT_2 + "," + windows1Seed + "," + 103);
                rawData.add(AGENT_2 + "," + windows1Seed + "," + 104);
                rawData.add(AGENT_2 + "," + windows1Seed + "," + 105);

                rawData.add(AGENT_3 + "," + windows1Seed + "," + 1001);
                rawData.add(AGENT_3 + "," + windows1Seed + "," + 1002);
                rawData.add(AGENT_3 + "," + windows1Seed + "," + 1003);
                rawData.add(AGENT_3 + "," + windows1Seed + "," + 1004);
                rawData.add(AGENT_3 + "," + windows1Seed + "," + 1005);

                rawData.add(AGENT_4 + "," + windows1Seed + "," + 10001);
                rawData.add(AGENT_4 + "," + windows1Seed + "," + 10002);
                rawData.add(AGENT_4 + "," + windows1Seed + "," + 10003);
                rawData.add(AGENT_4 + "," + windows1Seed + "," + 10004);
                rawData.add(AGENT_4 + "," + windows1Seed + "," + 10005);

                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100001));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100002));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100003));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100004));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100005));

                Long windows2Seed = 1000L;
                windows2Seed = windows2Seed + seed;
                rawData.add(AGENT_1 + "," + windows2Seed + "," + 11);
                rawData.add(AGENT_1 + "," + windows2Seed + "," + 12);
                rawData.add(AGENT_1 + "," + windows2Seed + "," + 13);
                rawData.add(AGENT_1 + "," + windows2Seed + "," + 14);
                rawData.add(AGENT_1 + "," + windows2Seed + "," + 15);

                rawData.add(AGENT_2 + "," + windows2Seed + "," + 111);
                rawData.add(AGENT_2 + "," + windows2Seed + "," + 112);
                rawData.add(AGENT_2 + "," + windows2Seed + "," + 113);
                rawData.add(AGENT_2 + "," + windows2Seed + "," + 114);
                rawData.add(AGENT_2 + "," + windows2Seed + "," + 115);

                rawData.add(AGENT_3 + "," + windows2Seed + "," + 1011);
                rawData.add(AGENT_3 + "," + windows2Seed + "," + 1012);
                rawData.add(AGENT_3 + "," + windows2Seed + "," + 1013);
                rawData.add(AGENT_3 + "," + windows2Seed + "," + 1014);
                rawData.add(AGENT_3 + "," + windows2Seed + "," + 1015);

                rawData.add(AGENT_4 + "," + windows2Seed + "," + 10011);
                rawData.add(AGENT_4 + "," + windows2Seed + "," + 10012);
                rawData.add(AGENT_4 + "," + windows2Seed + "," + 10013);
                rawData.add(AGENT_4 + "," + windows2Seed + "," + 10014);
                rawData.add(AGENT_4 + "," + windows2Seed + "," + 10015);

                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100011));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100012));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100013));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100014));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100015));

                //window3
                Long windows3Seed = 2000L;
                windows3Seed = windows3Seed + seed;
                rawData.add(AGENT_1 + "," + windows3Seed + "," + 21);
                rawData.add(AGENT_1 + "," + windows3Seed + "," + 22);
                rawData.add(AGENT_1 + "," + windows3Seed + "," + 23);
                rawData.add(AGENT_1 + "," + windows3Seed + "," + 24);
                rawData.add(AGENT_1 + "," + windows3Seed + "," + 25);

                rawData.add(AGENT_2 + "," + windows3Seed + "," + 121);
                rawData.add(AGENT_2 + "," + windows3Seed + "," + 122);
                rawData.add(AGENT_2 + "," + windows3Seed + "," + 123);
                rawData.add(AGENT_2 + "," + windows3Seed + "," + 124);
                rawData.add(AGENT_2 + "," + windows3Seed + "," + 125);

                rawData.add(AGENT_3 + "," + windows3Seed + "," + 1021);
                rawData.add(AGENT_3 + "," + windows3Seed + "," + 1022);
                rawData.add(AGENT_3 + "," + windows3Seed + "," + 1023);
                rawData.add(AGENT_3 + "," + windows3Seed + "," + 1024);
                rawData.add(AGENT_3 + "," + windows3Seed + "," + 1025);

                rawData.add(AGENT_4 + "," + windows3Seed + "," + 10021);
                rawData.add(AGENT_4 + "," + windows3Seed + "," + 10022);
                rawData.add(AGENT_4 + "," + windows3Seed + "," + 10023);
                rawData.add(AGENT_4 + "," + windows3Seed + "," + 10024);
                rawData.add(AGENT_4 + "," + windows3Seed + "," + 10025);

                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100021));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100022));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100023));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100024));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100025));

                //window4
                Long windows4Seed = 3000L;
                windows4Seed = windows4Seed + seed;
                rawData.add(AGENT_1 + "," + windows4Seed + "," + 31);
                rawData.add(AGENT_1 + "," + windows4Seed + "," + 32);
                rawData.add(AGENT_1 + "," + windows4Seed + "," + 33);
                rawData.add(AGENT_1 + "," + windows4Seed + "," + 34);
                rawData.add(AGENT_1 + "," + windows4Seed + "," + 35);

                rawData.add(AGENT_2 + "," + windows4Seed + "," + 131);
                rawData.add(AGENT_2 + "," + windows4Seed + "," + 132);
                rawData.add(AGENT_2 + "," + windows4Seed + "," + 133);
                rawData.add(AGENT_2 + "," + windows4Seed + "," + 134);
                rawData.add(AGENT_2 + "," + windows4Seed + "," + 135);

                rawData.add(AGENT_3 + "," + windows4Seed + "," + 1031);
                rawData.add(AGENT_3 + "," + windows4Seed + "," + 1032);
                rawData.add(AGENT_3 + "," + windows4Seed + "," + 1033);
                rawData.add(AGENT_3 + "," + windows4Seed + "," + 1034);
                rawData.add(AGENT_3 + "," + windows4Seed + "," + 1035);

                rawData.add(AGENT_4 + "," + windows4Seed + "," + 10031);
                rawData.add(AGENT_4 + "," + windows4Seed + "," + 10032);
                rawData.add(AGENT_4 + "," + windows4Seed + "," + 10033);
                rawData.add(AGENT_4 + "," + windows4Seed + "," + 10034);
                rawData.add(AGENT_4 + "," + windows4Seed + "," + 10035);

                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100031));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100032));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100033));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100034));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100035));

                //window 5
                Long windows5Seed = 4000L;
                windows5Seed = windows5Seed + seed;
                rawData.add(AGENT_1 + "," + windows5Seed + "," + 41);
                rawData.add(AGENT_1 + "," + windows5Seed + "," + 42);
                rawData.add(AGENT_1 + "," + windows5Seed + "," + 43);
                rawData.add(AGENT_1 + "," + windows5Seed + "," + 44);
                rawData.add(AGENT_1 + "," + windows5Seed + "," + 45);

                rawData.add(AGENT_2 + "," + windows5Seed + "," + 141);
                rawData.add(AGENT_2 + "," + windows5Seed + "," + 142);
                rawData.add(AGENT_2 + "," + windows5Seed + "," + 143);
                rawData.add(AGENT_2 + "," + windows5Seed + "," + 144);
                rawData.add(AGENT_2 + "," + windows5Seed + "," + 145);

                rawData.add(AGENT_3 + "," + windows5Seed + "," + 1041);
                rawData.add(AGENT_3 + "," + windows5Seed + "," + 1042);
                rawData.add(AGENT_3 + "," + windows5Seed + "," + 1043);
                rawData.add(AGENT_3 + "," + windows5Seed + "," + 1044);
                rawData.add(AGENT_3 + "," + windows5Seed + "," + 1045);

                rawData.add(AGENT_4 + "," + windows5Seed + "," + 10041);
                rawData.add(AGENT_4 + "," + windows5Seed + "," + 10042);
                rawData.add(AGENT_4 + "," + windows5Seed + "," + 10043);
                rawData.add(AGENT_4 + "," + windows5Seed + "," + 10044);
                rawData.add(AGENT_4 + "," + windows5Seed + "," + 10045);

                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100041));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100042));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100043));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100044));
                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100045));

                return rawData;
                }







//        public List<String> getRawData() {
//                final String AGENT_1 = "agent1";
//                final String AGENT_2 = "agent2";
//                final String AGENT_3 = "agent3";
//                final String AGENT_4 = "agent4";
//                final String AGENT_5 = "agent5";
//
//                List<String> rawData = new ArrayList<>();
//                // window 1
//                rawData.add(AGENT_1 + ",100," + 1);
//                rawData.add(AGENT_1 + ",100," + 2);
//                rawData.add(AGENT_1 + ",100," + 3);
//                rawData.add(AGENT_1 + ",100," + 4);
//                rawData.add(AGENT_1 + ",100," + 5);
//
//                rawData.add(AGENT_2 + ",100," + 101);
//                rawData.add(AGENT_2 + ",100," + 102);
//                rawData.add(AGENT_2 + ",100," + 103);
//                rawData.add(AGENT_2 + ",100," + 104);
//                rawData.add(AGENT_2 + ",100," + 105);
//
//                rawData.add(AGENT_3 + ",100," + 1001);
//                rawData.add(AGENT_3 + ",100," + 1002);
//                rawData.add(AGENT_3 + ",100," + 1003);
//                rawData.add(AGENT_3 + ",100," + 1004);
//                rawData.add(AGENT_3 + ",100," + 1005);
//
//                rawData.add(AGENT_4 + ",100," + 10001);
//                rawData.add(AGENT_4 + ",100," + 10002);
//                rawData.add(AGENT_4 + ",100," + 10003);
//                rawData.add(AGENT_4 + ",100," + 10004);
//                rawData.add(AGENT_4 + ",100," + 10005);
//
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100001));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100002));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100003));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100004));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100005));
//
//                //window2
//                rawData.add(AGENT_1 + ",1100," + 11);
//                rawData.add(AGENT_1 + ",1100," + 12);
//                rawData.add(AGENT_1 + ",1100," + 13);
//                rawData.add(AGENT_1 + ",1100," + 14);
//                rawData.add(AGENT_1 + ",1100," + 15);
//
//                rawData.add(AGENT_2 + ",1100," + 111);
//                rawData.add(AGENT_2 + ",1100," + 112);
//                rawData.add(AGENT_2 + ",1100," + 113);
//                rawData.add(AGENT_2 + ",1100," + 114);
//                rawData.add(AGENT_2 + ",1100," + 115);
//
//                rawData.add(AGENT_3 + ",1100," + 1011);
//                rawData.add(AGENT_3 + ",1100," + 1012);
//                rawData.add(AGENT_3 + ",1100," + 1013);
//                rawData.add(AGENT_3 + ",1100," + 1014);
//                rawData.add(AGENT_3 + ",1100," + 1015);
//
//                rawData.add(AGENT_4 + ",1100," + 10011);
//                rawData.add(AGENT_4 + ",1100," + 10012);
//                rawData.add(AGENT_4 + ",1100," + 10013);
//                rawData.add(AGENT_4 + ",1100," + 10014);
//                rawData.add(AGENT_4 + ",1100," + 10015);
//
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100011));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100012));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100013));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100014));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100015));
//
//                //window3
//                rawData.add(AGENT_1 + ",2100," + 21);
//                rawData.add(AGENT_1 + ",2100," + 22);
//                rawData.add(AGENT_1 + ",2100," + 23);
//                rawData.add(AGENT_1 + ",2100," + 24);
//                rawData.add(AGENT_1 + ",2100," + 25);
//
//                rawData.add(AGENT_2 + ",2100," + 121);
//                rawData.add(AGENT_2 + ",2100," + 122);
//                rawData.add(AGENT_2 + ",2100," + 123);
//                rawData.add(AGENT_2 + ",2100," + 124);
//                rawData.add(AGENT_2 + ",2100," + 125);
//
//                rawData.add(AGENT_3 + ",2100," + 1021);
//                rawData.add(AGENT_3 + ",2100," + 1022);
//                rawData.add(AGENT_3 + ",2100," + 1023);
//                rawData.add(AGENT_3 + ",2100," + 1024);
//                rawData.add(AGENT_3 + ",2100," + 1025);
//
//                rawData.add(AGENT_4 + ",2100," + 10021);
//                rawData.add(AGENT_4 + ",2100," + 10022);
//                rawData.add(AGENT_4 + ",2100," + 10023);
//                rawData.add(AGENT_4 + ",2100," + 10024);
//                rawData.add(AGENT_4 + ",2100," + 10025);
//
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100021));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100022));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100023));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100024));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100025));
//
//                //window4
//                rawData.add(AGENT_1 + ",3100," + 31);
//                rawData.add(AGENT_1 + ",3100," + 32);
//                rawData.add(AGENT_1 + ",3100," + 33);
//                rawData.add(AGENT_1 + ",3100," + 34);
//                rawData.add(AGENT_1 + ",3100," + 35);
//
//                rawData.add(AGENT_2 + ",3100," + 131);
//                rawData.add(AGENT_2 + ",3100," + 132);
//                rawData.add(AGENT_2 + ",3100," + 133);
//                rawData.add(AGENT_2 + ",3100," + 134);
//                rawData.add(AGENT_2 + ",3100," + 135);
//
//                rawData.add(AGENT_3 + ",3100," + 1031);
//                rawData.add(AGENT_3 + ",3100," + 1032);
//                rawData.add(AGENT_3 + ",3100," + 1033);
//                rawData.add(AGENT_3 + ",3100," + 1034);
//                rawData.add(AGENT_3 + ",3100," + 1035);
//
//                rawData.add(AGENT_4 + ",3100," + 10031);
//                rawData.add(AGENT_4 + ",3100," + 10032);
//                rawData.add(AGENT_4 + ",3100," + 10033);
//                rawData.add(AGENT_4 + ",3100," + 10034);
//                rawData.add(AGENT_4 + ",3100," + 10035);
//
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100031));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100032));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100033));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100034));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100035));
//
//                //window 5
//                rawData.add(AGENT_1 + ",4100," + 41);
//                rawData.add(AGENT_1 + ",4100," + 42);
//                rawData.add(AGENT_1 + ",4100," + 43);
//                rawData.add(AGENT_1 + ",4100," + 44);
//                rawData.add(AGENT_1 + ",4100," + 45);
//
//                rawData.add(AGENT_2 + ",4100," + 141);
//                rawData.add(AGENT_2 + ",4100," + 142);
//                rawData.add(AGENT_2 + ",4100," + 143);
//                rawData.add(AGENT_2 + ",4100," + 144);
//                rawData.add(AGENT_2 + ",4100," + 145);
//
//                rawData.add(AGENT_3 + ",4100," + 1041);
//                rawData.add(AGENT_3 + ",4100," + 1042);
//                rawData.add(AGENT_3 + ",4100," + 1043);
//                rawData.add(AGENT_3 + ",4100," + 1044);
//                rawData.add(AGENT_3 + ",4100," + 1045);
//
//                rawData.add(AGENT_4 + ",4100," + 10041);
//                rawData.add(AGENT_4 + ",4100," + 10042);
//                rawData.add(AGENT_4 + ",4100," + 10043);
//                rawData.add(AGENT_4 + ",4100," + 10044);
//                rawData.add(AGENT_4 + ",4100," + 10045);
//
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100041));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100042));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100043));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100044));
//                //        rawData.add(new Tuple2<String, Integer>(AGENT_5, 100045));
//
//                return rawData;
//                }


}
