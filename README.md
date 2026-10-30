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

