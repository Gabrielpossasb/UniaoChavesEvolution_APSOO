package uniaoevolutions.negocio;

/** Serviço ou peça do catálogo (TB_ServicoBase no MOR). */
public class ServicoBase {

    private int idServicoBase;
    private String descricao;
    private String categoria;
    private String observacoesTecnicas;

    public ServicoBase() {
    }

    public ServicoBase(int idServicoBase, String descricao, String categoria, String observacoesTecnicas) {
        this.idServicoBase = idServicoBase;
        this.descricao = descricao;
        this.categoria = categoria;
        this.observacoesTecnicas = observacoesTecnicas;
    }

    public int getIdServicoBase() {
        return idServicoBase;
    }

    public void setIdServicoBase(int idServicoBase) {
        this.idServicoBase = idServicoBase;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getObservacoesTecnicas() {
        return observacoesTecnicas;
    }

    public void setObservacoesTecnicas(String observacoesTecnicas) {
        this.observacoesTecnicas = observacoesTecnicas;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
