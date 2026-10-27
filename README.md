# ALTIV Afiliados IA

Aplicativo Android para divulgação de produtos de afiliados.

## Beta 9 — Shopee sem API

O botão "Acessar Shopee sem login ALTIV" abre a Central Shopee sem
necessidade de cadastro no sistema ALTIV.

1. Toque em "Abrir portal de afiliados". O portal será aberto no navegador
   seguro do Android, dentro de uma aba integrada (Custom Tabs).
2. Faça o login na Shopee e gere o seu link personalizado na ferramenta oficial.
3. Copie o link e toque em "Colar link copiado", ou compartilhe o link pelo
   Android, escolhendo "ALTIV • Shopee".
4. Digite o produto e, se desejar, o preço. Monte a legenda e revise.
5. Toque em "Compartilhar no WhatsApp", selecione os grupos ou contatos
   e confirme a publicação manualmente.

A Beta 9 não lê senha/cookies Shopee, não varre produtos, não cria links
de afiliado automaticamente, não dispara mensagens automaticamente
em grupos e não usa IA/Qwen para geração de legenda. O gerador usa um
modelo local de texto. Links são validados pelo domínio HTTPS; isso não
comprova que o link está vinculado ao seu cadastro de afiliado.

Os rascunhos ficam salvos somente no celular. Não houve alteração no
Supabase nem nas funcionalidades existentes de login e ranking.
A navegação integrada depende de navegador compatível instalado.

## Compilação

JDK 17, Android SDK 35 e Gradle 8.10.2. Na pasta android,
execute gradle assembleDebug. Saída: app/build/outputs/apk/debug/app-debug.apk.

O workflow de Beta 9 verifica links Shopee e compila no GitHub Actions.
Depois de merge em main, cria uma pré-release numerada sem apagar Beta 8.
O APK de debug é voltado a testes, não a publicação na Play Store.

## Verificação funcional no aparelho

- Abrir a Central Shopee sem login ALTIV.
- Autenticar no portal oficial (testar inclusive login Google se usado).
- Copiar um link personalizado e importar.
- Compartilhar pela folha de compartilhamento do Android para o ALTIV.
- Rejeitar domínios falsos e URLs HTTP.
- Criar legenda, salvar e recuperar rascunho.
- Compartilhar para um grupo WhatsApp de teste, confirmando envio.
- Verificar login e navegação antiga para detectar regressões.
