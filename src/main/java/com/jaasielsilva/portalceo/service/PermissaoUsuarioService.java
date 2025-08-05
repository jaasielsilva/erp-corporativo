package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissaoUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Verifica se um usuário pode editar outro usuário
     * @param editorId ID do usuário que quer editar
     * @param alvoId ID do usuário a ser editado
     * @return true se pode editar
     */
    public boolean podeEditarUsuario(Long editorId, Long alvoId) {
        if (editorId.equals(alvoId)) {
            return true; // Usuário pode editar a si mesmo (com limitações)
        }

        Optional<Usuario> editor = usuarioRepository.findById(editorId);
        Optional<Usuario> alvo = usuarioRepository.findById(alvoId);

        if (editor.isEmpty() || alvo.isEmpty()) {
            return false;
        }

        Usuario usuarioEditor = editor.get();
        Usuario usuarioAlvo = alvo.get();

        // MASTER nunca pode ser editado por ninguém (exceto ele mesmo)
        if (usuarioAlvo.getNivelAcesso() == NivelAcesso.MASTER && !editorId.equals(alvoId)) {
            return false;
        }

        // Verifica se o editor tem autoridade sobre o alvo
        return usuarioEditor.getNivelAcesso().temAutoridadeSobre(usuarioAlvo.getNivelAcesso());
    }

    /**
     * Verifica se um usuário pode excluir outro usuário
     * @param editorId ID do usuário que quer excluir
     * @param alvoId ID do usuário a ser excluído
     * @return true se pode excluir
     */
    public boolean podeExcluirUsuario(Long editorId, Long alvoId) {
        if (editorId.equals(alvoId)) {
            return false; // Usuário não pode se excluir
        }

        Optional<Usuario> editor = usuarioRepository.findById(editorId);
        Optional<Usuario> alvo = usuarioRepository.findById(alvoId);

        if (editor.isEmpty() || alvo.isEmpty()) {
            return false;
        }

        Usuario usuarioEditor = editor.get();
        Usuario usuarioAlvo = alvo.get();

        // MASTER nunca pode ser excluído
        if (usuarioAlvo.getNivelAcesso() == NivelAcesso.MASTER) {
            return false;
        }

        // Apenas MASTER e ADMIN podem excluir usuários
        if (!usuarioEditor.getNivelAcesso().podeGerenciarUsuarios()) {
            return false;
        }

        // Verifica se o editor tem autoridade sobre o alvo
        return usuarioEditor.getNivelAcesso().temAutoridadeSobre(usuarioAlvo.getNivelAcesso());
    }

    /**
     * Verifica se um usuário pode alterar o nível de acesso de outro
     * @param editorId ID do usuário que quer alterar
     * @param alvoId ID do usuário a ter o nível alterado
     * @param novoNivel Novo nível de acesso
     * @return true se pode alterar
     */
    public boolean podeAlterarNivelAcesso(Long editorId, Long alvoId, NivelAcesso novoNivel) {
        Optional<Usuario> editor = usuarioRepository.findById(editorId);
        Optional<Usuario> alvo = usuarioRepository.findById(alvoId);

        if (editor.isEmpty() || alvo.isEmpty()) {
            return false;
        }

        Usuario usuarioEditor = editor.get();
        Usuario usuarioAlvo = alvo.get();

        // MASTER nunca pode ter o nível alterado
        if (usuarioAlvo.getNivelAcesso() == NivelAcesso.MASTER) {
            return false;
        }

        // Não pode promover alguém para MASTER
        if (novoNivel == NivelAcesso.MASTER) {
            return false;
        }

        // Apenas MASTER pode criar outros ADMINs
        if (novoNivel == NivelAcesso.ADMIN && usuarioEditor.getNivelAcesso() != NivelAcesso.MASTER) {
            return false;
        }

        // Editor deve ter autoridade sobre o nível atual do alvo
        if (!usuarioEditor.getNivelAcesso().temAutoridadeSobre(usuarioAlvo.getNivelAcesso())) {
            return false;
        }

        // Editor deve ter autoridade sobre o novo nível
        return usuarioEditor.getNivelAcesso().temAutoridadeSobre(novoNivel);
    }

    /**
     * Verifica se um usuário pode acessar uma funcionalidade específica
     * @param usuarioId ID do usuário
     * @param funcionalidade Nome da funcionalidade
     * @return true se pode acessar
     */
    public boolean podeAcessarFuncionalidade(Long usuarioId, String funcionalidade) {
        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            return false;
        }

        NivelAcesso nivel = usuario.get().getNivelAcesso();

        return switch (funcionalidade.toLowerCase()) {
            case "usuarios", "gerenciar_usuarios" -> nivel.podeGerenciarUsuarios();
            case "financeiro", "relatorios_financeiros" -> nivel.podeAcessarFinanceiro();
            case "rh", "recursos_humanos" -> nivel.podeGerenciarRH();
            case "configuracoes" -> nivel.ehAdministrativo();
            case "permissoes" -> nivel.ehAdministrativo();
            case "dashboard" -> true; // Todos podem acessar
            case "meu_perfil" -> true; // Todos podem acessar
            default -> nivel.ehGerencial(); // Funcionalidades não mapeadas requerem nível gerencial
        };
    }

    /**
     * Verifica se um usuário pode ver informações de outro usuário
     * @param visualizadorId ID do usuário que quer visualizar
     * @param alvoId ID do usuário a ser visualizado
     * @return true se pode visualizar
     */
    public boolean podeVisualizarUsuario(Long visualizadorId, Long alvoId) {
        if (visualizadorId.equals(alvoId)) {
            return true; // Usuário pode ver suas próprias informações
        }

        Optional<Usuario> visualizador = usuarioRepository.findById(visualizadorId);
        if (visualizador.isEmpty()) {
            return false;
        }

        NivelAcesso nivel = visualizador.get().getNivelAcesso();
        
        // Níveis gerenciais podem visualizar informações de outros usuários
        return nivel.ehGerencial();
    }

    /**
     * Retorna o nível máximo que um usuário pode atribuir a outros
     * @param usuarioId ID do usuário
     * @return Nível máximo que pode atribuir
     */
    public NivelAcesso getNivelMaximoParaAtribuir(Long usuarioId) {
        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            return NivelAcesso.VISITANTE;
        }

        NivelAcesso nivelUsuario = usuario.get().getNivelAcesso();
        
        return switch (nivelUsuario) {
            case MASTER -> NivelAcesso.ADMIN; // MASTER pode criar até ADMIN
            case ADMIN -> NivelAcesso.GERENTE; // ADMIN pode criar até GERENTE
            case GERENTE -> NivelAcesso.COORDENADOR; // GERENTE pode criar até COORDENADOR
            case COORDENADOR -> NivelAcesso.SUPERVISOR; // COORDENADOR pode criar até SUPERVISOR
            case SUPERVISOR -> NivelAcesso.ANALISTA; // SUPERVISOR pode criar até ANALISTA
            default -> NivelAcesso.USER; // Outros podem criar apenas USER
        };
    }

    /**
     * Verifica se um usuário é protegido (não pode ser editado/excluído)
     * @param usuarioId ID do usuário
     * @return true se é protegido
     */
    public boolean ehUsuarioProtegido(Long usuarioId) {
        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            return false;
        }

        return usuario.get().getNivelAcesso().ehProtegido();
    }
}