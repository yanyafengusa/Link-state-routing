import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class lsr {
	public static int count = 0;
	public static Map<Integer, router> routers = new HashMap<Integer, router>();
	public static String[] s = null;
	public static double INF = Double.MAX_VALUE;
	public static double INF1 = 999999999;
	public static List<double[]> list = new ArrayList<double[]>();
	public static HashSet<String> set = new HashSet<String>();
	public static HashSet<Integer> setid = new HashSet<Integer>();
	public static boolean flag=true;

	public static class router {
		public int id;
		String network;
		boolean status = true;
		public int storeTTL;
		Map<Integer, Integer> id_order = new HashMap<Integer, Integer>();
		Map<Integer, Integer> order_id = new HashMap<Integer, Integer>();
		List<Routedge> adjedges = new ArrayList<Routedge>();
		List<Routedge> edges = new ArrayList<Routedge>();
		List<Integer> information = new ArrayList<Integer>();
		List<Integer> directrouters = new ArrayList<Integer>();
		List<Routedge> graph = new ArrayList<Routedge>();

		public router(int routerid, String address) {
			this.id = routerid;
			this.network = address;
		}

		public void edges(Routedge edge) {
			this.edges.add(edge);
		}

		public void graph(List<Routedge> edge) {
			this.graph.addAll(edge);
			this.graph = new ArrayList<Routedge>(new HashSet<Routedge>(this.graph));
		}

		public void orders() {
			int order = 0;
			this.id_order.clear();
			this.order_id.clear();
			this.adjedges.removeAll(this.adjedges);
			for (Routedge edge : this.graph) {
				if(routers.get(edge.start).status == false||routers.get(edge.end).status == false){
					Routedge edgexx = new Routedge(edge.start,edge.end,INF1);
					this.adjedges.add(edgexx);
				}
				else{
					this.adjedges.add(edge);
				}
				if (!id_order.containsKey(edge.start)) {
					id_order.put(edge.start, order);
					order_id.put(order, edge.start);
					order++;
				}
				if (!id_order.containsKey(edge.end)) {
					id_order.put(edge.end, order);
					order_id.put(order, edge.end);
					order++;
				}
			}
		}

		public void originatePacket() {
			if (this.status == true) {
				for (int routerid : directrouters) {
					lsp newlsp = new lsp(this.id, this.edges, this.id);
					if (routers.get(routerid).status == true) {
						routers.get(routerid).receivePacket(newlsp);
					}
				}
			}
		}

		public void receivePacket(lsp lsp) {
			lsp.TTL--;
			this.storeTTL = lsp.TTL;
			if (lsp.TTL > 0 && !this.information.contains(lsp.originalid)) {
				this.information.add(lsp.originalid);
				this.graph(lsp.tempedges);
				for (int routerid : directrouters) {
					lsp.TTL = this.storeTTL;
					if (routers.get(routerid).status == true && routerid != lsp.lastid && routerid != lsp.originalid) {
						lsp.lastid = this.id;
						routers.get(routerid).receivePacket(lsp);
					}
				}
			}
		}
	}

	public static class Routedge {
		int start;
		int end;
		double cost;

		public Routedge(int start, int end, double cost) {
			this.start = start;
			this.end = end;
			this.cost = cost;
		}
	}

	public static class lsp {
		int originalid;
		int lastid;
		int TTL = 10;
		List<Routedge> tempedges = new ArrayList<Routedge>();

		public lsp(int id, List<Routedge> edges, int idd) {
			this.originalid = id;
			this.lastid = idd;
			this.tempedges = edges;
		}
	}

	public static void main(String[] args) {
		buildGraph1();
		if (buildGraph() == true && flag==true) {
			for (router router : routers.values()) {
				router.orders();
			}
			Scanner sc = new Scanner(System.in);
			while (true) {
				System.out.println("please enter the following: (Tip: enter C to update after enter S or T)");
				System.out.println("C to continue");
				System.out.println("Q to quit");
				System.out.println("P # to print");
				System.out.println("S # to shut down");
				System.out.println("T # to start up");
				String input = sc.nextLine();
				
				if (input.toLowerCase().equals("c")) {
					for (router router : routers.values()) {
						router.originatePacket();
					}
					for (router router : routers.values()) {
						router.orders();
					}
					continue;
				} 
				
				else if (input.toLowerCase().equals("q")) {
					System.out.println("You ended the code.");
					sc.close();
					System.exit(0);
				} 
				
				else if (((input.charAt(0) == 'p' || input.charAt(0) == 'P') && input.length() >= 2
						&& input.replace("p", "").replace("P", "").replaceAll(" ", "").matches("[0-9]*")
						&& !input.replace("p", "").replace("P", "").replaceAll(" ", "").equals(""))) {
					int id = Integer.parseInt(input.replace("p", "").replace("P", "").replaceAll(" ", ""));
					
					if (!setid.contains(id)) {
						System.out.println("enter error!! enter again!");
						continue;
					}
					
					if (routers.get(id).status == false) {
						System.out.println("this router has been shut down");
					}
					
					
					else {
						for (Routedge edge : routers.get(id).adjedges) {
							double[] a = { (double) routers.get(id).id_order.get(edge.start),
									(double) routers.get(id).id_order.get(edge.end), edge.cost };
							list.add(a);
						}
						if (list.size() == 0) {
							System.out.println("network	          cost        outgoing link");
							System.out.printf(routers.get(id).network  + "        " + 0 + "         "
									+ routers.get(id).network);
							System.out.println();
						} else {
							int[] prev = new int[routers.get(id).id_order.size()];
							double[] dist = new double[routers.get(id).id_order.size()];
							preindex(routers.get(id).id_order.get(id), prev, dist, routers.get(id));
						}
					}
					list.removeAll(list);
					continue;
				} 
				
				else if (((input.charAt(0) == 's' || input.charAt(0) == 'S') && input.length() >= 2
						&& input.replace("s", "").replace("S", "").replaceAll(" ", "").matches("[0-9]*")
						&& !input.replace("s", "").replace("S", "").replaceAll(" ", "").equals(""))) {
					int id = Integer.parseInt(input.replace("s", "").replace("S", "").replaceAll(" ", ""));
					if (!setid.contains(id)) {
						System.out.println("enter error! enter again!");
						continue;
					}
					routers.get(id).status = false;
					continue;
				} 
				
				else if (((input.charAt(0) == 't' || input.charAt(0) == 'T') && input.length() >= 2
						&& input.replace("t", "").replace("T", "").replaceAll(" ", "").matches("[0-9]*")
						&& !input.replace("t", "").replace("T", "").replaceAll(" ", "").equals(""))) {
					int id = Integer.parseInt(input.replace("t", "").replace("T", "").replaceAll(" ", ""));
					if (!setid.contains(id)) {
						System.out.println("enter error! enter again!");
						continue;
					}
					routers.get(id).status = true;
					continue;
				} 
				
				else {
					System.out.println("enter error! enter again!");
					continue;
				}
			}
		}
	}

	private static boolean buildGraph() {
		try {
			File file = new File("infile.dat");
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String line = null;
				ArrayList<String> lines = new ArrayList<String>();
				while ((line = bufferedReader.readLine()) != null) {
					lines.add(line);
				}
				read.close();

				for (int i = 0; i < lines.size(); i++) {
					router newrouter = new router(0, "0");
					if (lines.get(i).charAt(0)!=32 && lines.get(i).charAt(0)!=9) {
						count++;
						String str = lines.get(i).trim();
						s = str.split("\\s+");
						newrouter.id = Integer.parseInt(s[0]);
						newrouter.network = s[1];
						set.add(s[1]);
						routers.put(Integer.parseInt(s[0]), newrouter);
					} else {
						String[] str = lines.get(i).trim().split("\\s+");
						if(str.length==2){
							Routedge newedge = new Routedge(Integer.parseInt(s[0]), Integer.parseInt(str[0]),
									Double.parseDouble(str[1]));
							routers.get(Integer.parseInt(s[0])).edges(newedge);
							routers.get(Integer.parseInt(s[0])).directrouters.add(Integer.parseInt(str[0]));
							routers.get(Integer.parseInt(s[0])).graph.add(newedge);
						}else{
							Routedge newedge = new Routedge(Integer.parseInt(s[0]), Integer.parseInt(str[0]),1.0);
							routers.get(Integer.parseInt(s[0])).edges(newedge);
							routers.get(Integer.parseInt(s[0])).directrouters.add(Integer.parseInt(str[0]));
							routers.get(Integer.parseInt(s[0])).graph.add(newedge);
						}
						
					}
				}
			} else {
				System.out.println("Can not find the file! Change path(line 231)");
				return false;
			}
		} catch (Exception e) {
			System.out.println("Read file error! Check the file again!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static HashSet<String> buildGraph1() {
		HashSet<String> getset = new HashSet<String>();
		try {
			File file = new File("infile.dat");
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				String line = null;
				ArrayList<String> lines = new ArrayList<String>();
				while ((line = bufferedReader.readLine()) != null) {
					lines.add(line);
				}
				read.close();
				for (int i = 0; i < lines.size(); i++) {
					if (lines.get(i).charAt(0)!=32 && lines.get(i).charAt(0)!=9) {
						String str = lines.get(i).trim();
						String[] strar = str.split("\\s+");
						setid.add(Integer.parseInt(strar[0]));
						getset.add(strar[1]);
					}
				}
			} else {
				System.out.println("Can not find the file! Change path(line 284)");
				flag=false;
			}
		} catch (Exception e) {
			System.out.println("Read file error! Check the file again!");
			e.printStackTrace();
			flag=false;
		}
		return getset;
	}

	public static double getWeight(int start, int end,router router) {
		if (start == end)
			return 0;
		for (int i = 0; i < list.size(); i++) {
			double[] a = list.get(i);
			double a1 = a[0];
			double a2 = a[1];
			double a3 = a[2];
			if ((start == a1 && end == a2) || (start == a2 && end == a1)) {
				return a3;
			}
		}
		return INF;
	}

	public static int[] dijkstra(int vs, int[] prev, double[] dist, router router) {
		boolean[] flag = new boolean[router.id_order.size()];
		for (int i = 0; i < router.id_order.size(); i++) {
			flag[i] = false;
			prev[i] = vs;
			dist[i] = getWeight(vs, i,router);
		}
		flag[vs] = true;
		dist[vs] = 0;
		int k = 0;
		for (int i = 1; i < router.id_order.size(); i++) {
			double min = INF;
			for (int j = 0; j < router.id_order.size(); j++) {
				if (flag[j] == false && dist[j] < min) {
					min = dist[j];
					k = j;
				}
			}
			flag[k] = true;
			for (int j = 0; j < router.id_order.size(); j++) {
				double tmp = getWeight(k, j,router);
				tmp = (tmp == INF ? INF : (min + tmp));
				if (flag[j] == false && (tmp < dist[j])) {
					dist[j] = tmp;
					prev[j] = k;
				}
			}
		}
		return prev;
	}

	public static void preindex(int vs, int[] prev, double[] dist, router router) {
		int[] prevv = dijkstra(vs, prev, dist, router);
		HashSet<String> getset = new HashSet<String>();
		for (int i = 0; i < router.id_order.size(); i++) {
			if (prevv[i] == vs) {
				prevv[i] = i;
			}
			while (getWeight(vs, prevv[i],router) == INF) {
				prevv[i] = prevv[prevv[i]];
			}
		}

		System.out.println(routers.get(router.order_id.get(vs)).network + ":");
		System.out.println(String.format("%-21s", " network")+String.format("%-20s", "cost")+String.format("%-20s", "outgoing link"));
		getset = buildGraph1();
		for (int i = 0; i < router.id_order.size(); i++) {
			if (i == vs) {
				prevv[i] = i;
			}
			if(dist[i]<INF1){
				System.out.printf(String.format("%-21s", routers.get(router.order_id.get(i)).network) 
						+ String.format("%-21s", dist[i])
						+ String.format("%-20s", routers.get(router.order_id.get(prev[i])).id));
				getset.remove(routers.get(router.order_id.get(i)).network);
				System.out.println();
			}
			else if(dist[i]>=INF1){
				System.out.printf(String.format("%-21s", routers.get(router.order_id.get(i)).network) 
						+ String.format("%-21s", "infinity" )
						+ String.format("%-20s", routers.get(router.order_id.get(prev[i])).id));
				getset.remove(routers.get(router.order_id.get(i)).network);
				System.out.println();
			}
		}
	}
}
