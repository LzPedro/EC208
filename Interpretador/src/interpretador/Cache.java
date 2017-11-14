/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interpretador;

/**
 *
 * @author aluno
 */
public class Cache {
    
    private final int NUM_PALAVRAS = 4;
    
    private boolean validade;
    private int[] palavra;
    private int dado;

    public Cache() {
        palavra = new int[NUM_PALAVRAS];
        validade = false;
        dado = 0;
        
        for (int i = 0; i < 10; i++) {
            palavra[i] = 0;
        }
    }

    public boolean isValidade() {
        return validade;
    }

    public void setValidade(boolean validade) {
        this.validade = validade;
    }

    public int[] getPalavra() {
        return palavra;
    }

    public void setPalavra(int[] palavra) {
        this.palavra = palavra;
    }

    public int getDado() {
        return dado;
    }

    public void setDado(int dado) {
        this.dado = dado;
    }
    
    
}
