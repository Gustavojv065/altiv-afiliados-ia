# ALTIV Afiliados IA — Beta 11

Aplicativo Android existente com novo **Radar Automático** de ofertas da Shopee.
O modo manual da Beta 10 continua disponível sem necessidade da Open API.

## O que a nova versão faz

- Tela Android nativa: configurar servidor, buscar ofertas por palavra-chave e desconto, ver preço e publicar com confirmação.
- Servidor Next.js: consulta a Open API oficial dos afiliados da Shopee Brasil; devolve **offerLink oficial já vinculado à conta**; não cria links com parâmetros falsos.
- Endpoint de publicação protegido: envia ao Telegram somente após autorização; identifica publicidade com `#publi`.
- GitHub Actions: publicação automática de até um produto por execução às 09h, 13h e 18h (Mato Grosso, UTC-4).
- Histórico dos IDs publicados por cache do GitHub Actions (melhor esforço por 30 dias; cache pode expirar/ser removido).
- Sem scraping, disparo automático a grupos de WhatsApp ou chamadas pagas de IA.

O código do fluxo API/Telegram utiliza como referência arquitetural [bot-achadinhos](https://github.com/vinniciusnascimento/bot-achadinhos), que consulta `productOfferV2` e publica no Telegram, e o agendamento do [shopee-telegram-bot](https://github.com/reoliveir4/shopee-telegram-bot). A implementação no ALTIV foi reescrita e usa o repositório Android existente.

## Configurar (obrigatório para pesquisa automática)

1. Solicite acesso **Open API de afiliados** no portal da Shopee, caso ainda não tenha credenciais.
2. Publique o Next.js em ambiente seguro (por exemplo Vercel, na sua conta). Configure as variáveis privadas de ambiente:
   - `SHOPEE_APP_ID`, `SHOPEE_APP_SECRET`: credenciais da **conta afiliada**, nunca embutidas no APK.
   - `ALTIV_ACCESS_TOKEN`: segredo aleatório forte, no mínimo 32 caracteres.
   - `TELEGRAM_BOT_TOKEN`: token do seu bot criado no BotFather.
   - `TELEGRAM_CHAT_ID`: canal ou grupo do qual seu bot é administrador.
3. Em **GitHub → Settings → Secrets and variables → Actions**, crie:
   - `ALTIV_BACKEND_URL`: URL HTTPS base da sua publicação web, sem barra final.
   - `ALTIV_ACCESS_TOKEN`: **o mesmo** token privado configurado na Vercel.
4. No aplicativo, abra **Achadinhos Shopee → NOVO • Radar automático de ofertas**. Insira a URL HTTPS e o token; toque em **Salvar e verificar**, depois em **Pesquisar produtos**.
5. Após merge na `main`, o workflow `Publicar ofertas Shopee (Telegram)` poderá executar nos horários configurados. Teste primeiro manualmente com **dry_run=true**. Para uma publicação real de teste, execute com **dry_run=false** apenas quando quiser enviar ao canal.

**Cuidado:** o workflow só funciona com o backend publicado e credenciais autorizadas. Se faltar configuração, não inventa ofertas. Se o bot não tiver permissão no canal, o Telegram recusará o envio.

## Proteção e limitações

- A API exige um token compartilhado de uso **individual**. Não é um backend multiusuário: para vender a terceiros, primeiro implementar autenticação e isolamento por cliente.
- O token usado no app fica nas preferências privadas do Android. Não compartilhar o APK já configurado, nem publicar tokens em arquivos/prints. Não habilitar backups de credenciais sem proteção adicional.
- GitHub Actions não garante execução no minuto exato e cache não é persistência transacional. Para produção com alto volume, migrar histórico/idempotência para banco controlado e agendamento persistente.
- Preços e comissões são estimativas fornecidas pela Shopee; confira antes da divulgação. Sem promessa de comissão.
- Compartilhamento WhatsApp continua assistido, com escolha e confirmação pelo usuário.
- Esta integração não altera Supabase nem precisa de Cloudflare.

## Construir APK

O projeto Android continua em `android/`, com JDK 17, SDK 35 e Gradle 8.10.2:
```sh
cd android
gradle --no-daemon assembleDebug
```
No PR, o workflow de Android compila um APK de teste em **Artifacts**. A release Beta 11 é criada pelo workflow **apenas após merge**, se o build passar. O APK não inclui chaves da Shopee ou Telegram.

## Testes necessários

- Login ALTIV/fluxo manual antigo permanecem operacionais; abrir novo Radar.
- Sem token → erro claro; token inválido → HTTP 401; sem credenciais Shopee → HTTP 503; offline → erro.
- Com API liberada: busca real por nicho, preços e `offerLink` oficial.
- Confirmar e cancelar envio manual; verificar produto e `#publi` no Telegram.
- Testar workflow `dry_run=true`; depois publicação real autorizada; repetição evitada no próximo ciclo.
- Conferir limites, permissões e termos da Shopee/Telegram antes de ativar agenda.


---

## Instruções legadas — Beta 10 (modo manual preservado)

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
