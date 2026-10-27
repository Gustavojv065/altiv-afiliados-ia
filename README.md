# ALTIV Afiliados IA

App Android para criar divulgações de produtos Shopee com sua conta de afiliado, sem exigir credenciais da API.

## Beta 10 — Compartilhar em um único fluxo

1. Em **Achadinhos Shopee**, toque em **Abrir aplicativo Shopee**.
2. No app oficial, entre em **Eu → Criadores e Afiliados → Recomendação**.
3. Escolha uma oferta; confirme os dados do produto e gere um link válido da **sua conta de afiliado**.
4. No compartilhamento da Shopee, escolha **Copiar Informações** ou compartilhe o texto para **ALTIV • Shopee**.
5. Volte ao ALTIV e toque em **Colar informações da Shopee** (ou deixe que o app receba o compartilhamento).
6. O texto, preço e link são interpretados localmente e a **prévia é criada com um único link**.
7. Revise a mensagem e toque em **Compartilhar no WhatsApp**. Selecione manualmente os grupos e confirme o envio.

A prévia é editável e o aplicativo preserva apenas uma URL Shopee na mensagem final, mesmo quando o texto recebido contém links repetidos. O usuário pode guardar um rascunho no aparelho. A Beta 10 migra o rascunho da Beta 9 ao abrir a tela pela primeira vez, se houver conteúdo.

### Acesso à Shopee

A abertura do aplicativo oficial `com.shopee.br` é pela tela inicial pública instalada no celular. Não é possível garantir um atalho para a área privada de afiliados sem um deep link oficial documentado; siga o caminho **Eu → Criadores e Afiliados**. Se o aplicativo não estiver disponível, o ALTIV abre o portal `https://affiliate.shopee.com.br/` em Custom Tabs.

### Limites e privacidade

- Não lê cookies/senhas nem controla a sessão na Shopee.
- Não faz scraping ou busca automática de ranking/comissão. A descoberta ocorre pela área Recomendação da Shopee; futuros filtros inteligentes podem usar dados fornecidos voluntariamente pelo usuário ou API autorizada.
- Não gera/converte links afiliados de forma automática, nem garante vínculo do link com sua conta.
- Não integra Qwen ou IA online nesta Beta. A prévia reutiliza as informações copiadas da Shopee e acrescenta uma única URL e `#publi` (sem inventar preços).
- Não automatiza disparos em grupos ou extrai contatos WhatsApp.
- Não altera Supabase: os rascunhos são locais e a área ALTIV existente continua separada.

## Compilar

JDK 17, Android SDK 35 e Gradle 8.10.2. Execute `gradle assembleDebug` na pasta `android/`.
O arquivo fica em `android/app/build/outputs/apk/debug/app-debug.apk`.
O workflow `.github/workflows/build-android-beta10.yml` testa o parser de links, a extração de informações e compila APK de debug. Após merge, publica pré-release Beta 10, sem apagar a Beta 9.

## Testes manuais necessários

- Abrir app Shopee instalado e entrar manualmente na área de afiliados.
- Copiar Informações de produto com preço e link; verificar um único link na prévia.
- Compartilhar diretamente texto Shopee para ALTIV.
- Testar cópia contendo vários links Shopee e hashtags repetidas.
- Rejeitar URLs HTTP ou domínios falsos.
- Editar a prévia, salvar e reabrir a tela.
- Confirmar que WhatsApp exige selecionar e confirmar grupo.
- Verificar login ALTIV, outras páginas e layout mobile.
- Preservar dados antes de reinstalar APK com assinatura diferente.

APK de debug: apenas para testes internos.
