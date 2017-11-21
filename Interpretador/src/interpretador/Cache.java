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
    private Palavra[] palavra;

    public Cache() {
        palavra = new Palavra[NUM_PALAVRAS];
        for (int i = 0; i < palavra.length; i++) {
            palavra[i] = new Palavra();
        }
    }

    public void inserePalavra(String dado, String endereco) {
        int tag = Integer.parseInt(endereco.substring(0, 2), 2);
        int pos = Integer.parseInt(endereco.substring(2, 4), 2);
        
        palavra[pos].setTag(tag);
        palavra[pos].setValidade(true);
        palavra[pos].setDado(dado);
    }
    
    public void invalidaDado(String endereco){
        int pos = Integer.parseInt(endereco.substring(2, 4), 2);
        
        palavra[pos].setValidade(false);
    }

    public String cacheSearch(String dado) {
        int pos = Integer.parseInt(dado.substring(2, 4), 2);
        int tag = Integer.parseInt(dado.substring(0, 2), 2);

        if (palavra[pos].isValidade()) {
            if (palavra[pos].getTag() == tag) {
                return palavra[pos].getDado();
            }
        }
        return null;
    }
}
