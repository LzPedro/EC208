/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interpretador;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Home
 */
public class Interpretador {

    //Registradores
    private static int rs;
    private static int rt;
    private static int rd;
    private static int pc;

    private static char format;
    private static int opcode;
    private static int shamt;
    private static int funct;
    private static int imm;
    private static int address;
    private static int[] general_register;

    //Escritores
    private static FileWriter fw;
    private static BufferedWriter bw;

    //Leitor de memoria
    private static final int BITSDADOS = 8;
    private static final int BITSENDERECO = 5;
    private static final int NUMPALAVRAS = 4;
    private static final int NUMENDERECOS = 32;
    private static RandomAccessFile mem;
    private static Cache[] cache;

    public static void main(String[] args) throws IOException {
        cache = new Cache[2];
        cache[0] = new Cache();
        cache[1] = new Cache();

        general_register = new int[32];
        for (int i = 0; i < general_register.length; i++) {
            general_register[i] = 0;
        }

        pc = 0;
        type(cache);
    }

    //Identifica o tipo de codigo (R, I ou J)
    private static void type(Cache[] cache) throws IOException {
        /*
            6 primeiros bits correspondem ao tipo de codigo
            
            0 - Tipo R
            2 ou 3 - Tipo J
            Outros - Tipo I
         */
        String code;
        RandomAccessFile raf = new RandomAccessFile("operacao.txt", "r");

        raf.seek(0);
        while ((code = raf.readLine()) != null) {
            pc += code.length() + 2;
            raf.seek(pc);

            code = code.replaceAll(" ", "");
            String subCode = code.substring(0, 6);
            opcode = Integer.parseInt(subCode, 2);

            switch (opcode) {
                case 0:
                    format = 'R';
                    break;
                case 2:
                case 3:
                    format = 'J';
                    break;
                default:
                    format = 'I';
                    break;
            }
            breakCode(code, cache);
        }
    }

    //Separa o codigo em partes de acordo com o tipo de instrução
    private static void breakCode(String code, Cache[] cache) throws IOException {
        /*
            R - opcode(6) | rs(5) | rt(5) | rd(5) | shamt(5) | funct(6)
            I - opcode(6) | rs(5) | rt(5) | imm(16)
            J - opcode(6) | address(26)
         */

        switch (format) {
            case 'R':
                rs = Integer.parseInt(code.substring(6, 11), 2);
                rt = Integer.parseInt(code.substring(11, 16), 2);
                rd = Integer.parseInt(code.substring(16, 21), 2);
                shamt = Integer.parseInt(code.substring(21, 26), 2);
                funct = Integer.parseInt(code.substring(26, 32), 2);
                executeTypeR(cache);
                break;

            case 'J':
                address = Integer.parseInt(code.substring(6, 32), 2);
                break;

            default:
                rs = Integer.parseInt(code.substring(6, 11), 2);
                rt = Integer.parseInt(code.substring(11, 16), 2);
                imm = Integer.parseInt(code.substring(16, 32), 2);
                executeTypeI(cache);
                break;
        }
    }

    //Executa instruções do tipo R
    private static void executeTypeR(Cache[] cache) throws IOException {
        switch (funct) {
            case 32:
                add(rd, rs, rt);
                break;
            case 34:
                sub(rd, rs, rt);
                break;
            default:
                System.out.println("Invalid Operation");
                break;
        }
    }

    //Executa instruções do tipo I
    private static void executeTypeI(Cache[] cache) throws IOException {
        switch (opcode) {
            case 35:
                general_register[rt] = loadWord(rs, cache);
                break;
            case 43:
                storeWord(rs, rd, cache);
                break;
            default:
                System.out.println("Invalid Operation");
                break;
        }
    }

    private static int dataSearch(String dado, int registro, Cache[] cache) throws FileNotFoundException, IOException {
        int bloco = Integer.parseInt(dado.substring(4));
        String val = cache[bloco].cacheSearch(dado);

        if (val != null) {
            System.out.println("Cache Hit!");
        } else {
            System.out.println("Cache Miss!");

            mem = new RandomAccessFile("memoria.txt", "r");

            int palavrasPorRegiao = NUMENDERECOS / NUMPALAVRAS;
            int numRegioes = registro % palavrasPorRegiao;

            mem.seek((BITSDADOS + 2) * registro);
            val = mem.readLine();

            int posicao, j;
            
            if(registro < 8)            j = 0;
            else if(registro < 16)      j = 1;
            else if(registro < 24)      j = 2;
            else                        j = 3;
            
            posicao = (palavrasPorRegiao) * j + bloco; 
            for (int i = 0; i < NUMPALAVRAS; i++) {                 
                mem.seek((BITSDADOS + 2) * posicao);
                String valorCache = mem.readLine();
               
                String endereco = Integer.toBinaryString(posicao);
                if(endereco.length() < BITSENDERECO)    endereco = complete(endereco, BITSENDERECO);
                 
                cache[bloco].inserePalavra(valorCache, endereco);
                posicao += 2;
            }

            mem.close();
        }
        return Integer.parseInt(val, 2);
    }

    private static int loadWord(int registro, Cache[] cache) throws IOException {

        String dado = Integer.toBinaryString(registro);

        //Correção para que a string tenha a quantidade definida de bits
        if (dado.length() < BITSENDERECO) {
            dado = complete(dado, BITSENDERECO);
        }

        int val = dataSearch(dado, registro, cache);

        return val;
    }

    //escreve no txt de memoria o registro
    private static void storeWord(int registro, int valor, Cache[] cache) throws IOException {
        if (valor > Math.pow(2, BITSDADOS) - 1 || valor < 0) {
            System.out.println("Tamanho do espaço da memória excedido!!!");
            JOptionPane.showMessageDialog(null, "Tamanho da posição de memória excedido!!!");
        } else {
            String posicaoMemoria = Integer.toBinaryString(registro);
            if(posicaoMemoria.length() < BITSENDERECO)  posicaoMemoria = complete(posicaoMemoria, BITSENDERECO);

            int bloco = Integer.parseInt(posicaoMemoria.substring(4));
            cache[bloco].invalidaDado(posicaoMemoria);
            
            List<String> txt = new ArrayList<>();
             
            mem = new RandomAccessFile("memoria.txt", "r");
            String valorMemoria = mem.readLine();
            while(valorMemoria != null){
                txt.add(valorMemoria);
                valorMemoria = mem.readLine();
            }
            
            String val = Integer.toBinaryString(valor);
            if(val.length() < BITSDADOS)   complete(val, BITSDADOS);
            
            txt.set(registro, val);
            
            fw = new FileWriter("memoria.txt", false);
            bw = new BufferedWriter(fw);
            
            for (int i = 0; i < txt.size(); i++) {                
                if(i == txt.size() - 1){
                    bw.write(txt.get(i));
                }else{
                    bw.write(txt.get(i) + "\n");
                }
            }  
            bw.close();
        }

    }

    private static void add(int rd, int rs, int rt) throws IOException {
        //rd = rs + rt;
        rs = general_register[rs];
        rt = general_register[rt];
        Interpretador.rd = rs + rt;

    }

    private static void sub(int rd, int rs, int rt) throws IOException {
        //rd = rs + rt;
        rs = general_register[rs];
        rt = general_register[rt];
        Interpretador.rd = rs - rt;
    }

    private static String complete(String dado, int tam) {
        for (int j = dado.length(); j < tam; j++) {
            dado = "0" + dado;
        }
        return dado;
    }
}
