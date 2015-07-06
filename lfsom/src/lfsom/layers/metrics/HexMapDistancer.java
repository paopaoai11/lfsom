/*
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.livingforsom.com/license.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 */
package lfsom.layers.metrics;

import java.util.ArrayList;

/* @author vicente
 * @version $Id: $
 */
public class HexMapDistancer {

	private int[][][][] distancesHex = null;

	private int[][] AristasComp = null;

	private int[][] clusters = null;

	private int[][] subnets = null;

	private ArrayList<Integer> incluidos = null;

	private int xSize = 0;

	private boolean cargandoCluster = false;

	private boolean cargandoSubnet = false;

	private int ySize = 0;

	private ArrayList<Integer[]> lista;

	public HexMapDistancer(int xSiz, int ySiz, boolean simple) {

		xSize = xSiz;
		ySize = ySiz;

		distancesHex = new int[xSize][ySize][xSize][ySize];

		int[] increm = new int[ySize];
		increm[0] = 0;
		for (int i = 1; i < ySize; i++) {
			if (i % 2 != 0) {
				increm[i] = increm[i - 1] + 1;

			} else {
				increm[i] = increm[i - 1];
			}
		}
		// Tenemos en cuenta que se hace un offset de las pares
		// Tomamos como distancia la unidad y como offset el seno del �ngulo 60

		for (int x1 = 0; x1 < xSize; x1++) {
			for (int y1 = 0; y1 < ySize; y1++) {

				for (int x2 = 0; x2 < xSize; x2++) {
					for (int y2 = 0; y2 < ySize; y2++) {
						int dx = x2 + increm[y2] - x1 - increm[y1];
						int dist = Math.abs(dx);
						int dy = y2 - y1;
						int dif = Math.abs(dy - dx);
						if (Math.abs(dy) > dist) {
							dist = Math.abs(dy);
						}
						if (dif > dist) {
							dist = dif;
						}
						// Stefano MacGregor
						distancesHex[x1][y1][x2][y2] = dist;

					}
				}
			}
		}

	}

	public HexMapDistancer(int xSiz, int ySiz) {

		xSize = xSiz;
		ySize = ySiz;

		distancesHex = new int[xSize][ySize][xSize][ySize];
		AristasComp = new int[xSize * ySize][6];

		int[] increm = new int[ySize];
		increm[0] = 0;
		for (int i = 1; i < ySize; i++) {
			if (i % 2 != 0) {
				increm[i] = increm[i - 1] + 1;

			} else {
				increm[i] = increm[i - 1];
			}
		}
		// Tenemos en cuenta que se hace un offset de las pares
		// Tomamos como distancia la unidad y como offset el seno del �ngulo 60

		for (int x1 = 0; x1 < xSize; x1++) {
			for (int y1 = 0; y1 < ySize; y1++) {
				for (int k = 0; k < 6; k++) {
					AristasComp[x1 + y1 * xSize][k] = -1;
				}
				for (int x2 = 0; x2 < xSize; x2++) {
					for (int y2 = 0; y2 < ySize; y2++) {
						int dx = x2 + increm[y2] - x1 - increm[y1];
						int dist = Math.abs(dx);
						int dy = y2 - y1;
						int dif = Math.abs(dy - dx);
						if (Math.abs(dy) > dist) {
							dist = Math.abs(dy);
						}
						if (dif > dist) {
							dist = dif;
						}
						// Stefano MacGregor
						distancesHex[x1][y1][x2][y2] = dist;

						if (dist == 1) {
							int ind1 = x1 + y1 * xSize;
							int ind2 = x2 + y2 * xSize;
							int aris = -1;

							if (x2 < x1) {

								if (y2 == y1) {
									aris = 4;
								}
								if (y2 > y1) {
									aris = 5;
								}
								if (y2 < y1) {
									aris = 3;
								}
							}

							if (x2 == x1) {
								boolean par = y1 % 2 == 0;
								if (y2 > y1) {
									if (par) {
										aris = 0;
									} else {
										aris = 5;
									}
								}
								if (y2 < y1) {
									if (par) {
										aris = 2;
									} else {
										aris = 3;
									}
								}
							}

							if (x2 > x1) {
								if (y2 == y1) {
									aris = 1;
								}
								if (y2 > y1) {
									aris = 0;
								}
								if (y2 < y1) {
									aris = 2;
								}
							}

							AristasComp[ind1][aris] = ind2;

						}

					}
				}
			}
		}

	}

	public int[][][][] map() {
		return distancesHex;
	}

	public void cargaSubnets(float[] datSubnet) {
		cargandoSubnet = true;
		subnets = new int[xSize][ySize];
		int i = 0;
		for (int y = 0; y < ySize; y++) {
			for (int x = 0; x < xSize; x++) {

				subnets[x][y] = (int) datSubnet[i++];
			}
		}
		cargandoSubnet = false;
	}

	public void cargaClusters(float[] datCluster) {
		cargandoCluster = true;
		clusters = new int[xSize][ySize];
		int i = 0;
		for (int y = 0; y < ySize; y++) {
			for (int x = 0; x < xSize; x++) {

				clusters[x][y] = (int) datCluster[i++];
			}
		}
		cargandoCluster = false;
	}

	private ArrayList<Integer[]> calcLimites() {

		// Se marcan a 1 todas las aristas que no estan compartidas
		lista = new ArrayList<Integer[]>();
		for (int incluido : incluidos) {
			Integer[] display = new Integer[7];
			display[0] = incluido;
			for (int i = 1; i <= 6; i++) {
				if (incluidos.contains(AristasComp[incluido][i - 1])) {
					display[i] = 0;
				} else {
					display[i] = 1;
				}
			}
			lista.add(display);
		}

		return lista;
	}

	public ArrayList<Integer[]> listaProximos_display(int id, int radio) {
		listaProximos(id, radio);
		calcLimites();
		return lista;
	}

	public ArrayList<Integer[]> listaSubnet_display(int id) {
		// System.out.println("Inicio display");
		listaSubnet(id);
		// System.out.println("Calc lista");
		calcLimites();
		// System.out.println("A return");
		return lista;
	}

	public ArrayList<Integer[]> listaCluster_display(int id) {
		// System.out.println("Inicio display");
		listaCluster(id);
		// System.out.println("Calc lista");
		calcLimites();
		// System.out.println("A return");
		return lista;
	}

	public ArrayList<Integer[]> listaCustom_display(ArrayList<Integer> listaC) {
		listaCustom(listaC);
		calcLimites();
		return lista;
	}

	public ArrayList<Integer> getIncluidos() {
		return incluidos;
	}

	public ArrayList<Integer> traspon(ArrayList<Integer> lista) {
		ArrayList<Integer> traspu = new ArrayList<Integer>();
		int vacio = 0;
		int lleno = 1;

		Integer[][] mat1 = new Integer[ySize][xSize];
		Integer[][] mattrans = new Integer[ySize][xSize];

		int ntras = 0;
		for (int f = 0; f < ySize; f++) {
			for (int c = 0; c < xSize; c++) {
				mat1[f][c] = vacio;
				mattrans[f][c] = vacio;
				int coorde = f * xSize + c;
				if (lista.contains(coorde)) {
					mat1[f][c] = lleno;
				}
			}
		}

		// Ahora se recorre la matriz de arriba a abajo
		for (int c = 0; c < xSize; c++) {
			for (int f = 0; f < ySize; f++) {
				if (mat1[f][c] == lleno) {
					traspu.add(ntras);
				}
				ntras++;
			}
		}

		return traspu;
	}

	public void listaCluster(int id) {
		int filaac = id / xSize;
		int colac = id % xSize;
		incluidos = new ArrayList<Integer>();

		if (!cargandoCluster && clusters != null) {
			// System.out.println("Dentro lista1");
			int clusterac = clusters[colac][filaac];
			// System.out.println("Dentro lista2");
			for (int f = 0; f < ySize; f++) {
				for (int c = 0; c < xSize; c++) {
					if (clusters[c][f] == clusterac) {
						incluidos.add(f * xSize + c);
					}

				}
			}
		}

	}

	public void listaSubnet(int id) {
		int filaac = id / xSize;
		int colac = id % xSize;
		incluidos = new ArrayList<Integer>();

		if (!cargandoSubnet && subnets != null) {
			// System.out.println("Dentro lista1");
			int subnetac = subnets[colac][filaac];
			// System.out.println("Dentro lista2");
			for (int f = 0; f < ySize; f++) {
				for (int c = 0; c < xSize; c++) {
					if (subnets[c][f] == subnetac && subnets[c][f] > 0) {
						incluidos.add(f * xSize + c);
					}

				}
			}
		}

	}

	public void listaCustom(ArrayList<Integer> listaC) {

		incluidos = listaC;
	}

	public void listaProximos(int id, int radio) {
		int filaac = id / xSize;
		int colac = id % xSize;
		incluidos = new ArrayList<Integer>();

		for (int f = filaac - radio - 1; f < filaac + radio + 1; f++) {
			for (int c = colac - radio - 1; c < colac + radio + 1; c++) {
				if (f >= 0 && f < ySize && c >= 0 && c < xSize) {
					if (distancesHex[c][f][colac][filaac] <= radio) {
						incluidos.add(f * xSize + c);
					}
				}
			}
		}

	}

	/**
	 * @param incluidos2
	 * @return
	 */

}