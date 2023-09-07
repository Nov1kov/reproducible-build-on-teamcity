# Teamcity Kotlin DSL example

## How to run

1. Use: `docker-compose up -d` command.
2. The token is printed in the server console and `teamcity-server-logs/teamcity-server.log`
3. After creating a user, visit ["Agents -> Unauthorized"](http://localhost:8112/agents.html?tab=unauthorizedAgents) to authorize the build agent.
4. Go to the job [Generate Documentation](http://localhost:8112/buildConfiguration/WhatsappBusinessJavaApi_Build?mode=builds#all-projects) and click Run.
5. Optionally, you can run build with a specific commit hash. To do this, initiate the build with parameters, go to the "Changes" tab, select "Manual specific revision" under "Include changes," and paste the required commit hash into the "Revision" column.

## How it works.

Проект для сборки был выбран [whatsup-buisness-java-api](https://github.com/Bindambc/whatsapp-business-java-api)

### Project structure

- **.teamcity** - папка с Kotlin DSL скриптами.
- **teamcity-agent-conf** - папка с конфигами для агента teamcity.
- **teamcity-server-data** - папка с преднастроенными джобами в teamcity.
- **teamcity-server-logs** - здесь будут логи от teamcity server.
- **marketing-website** - nginx сервер с заготовленными данными.

