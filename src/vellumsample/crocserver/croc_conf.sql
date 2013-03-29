insert into config (group_, name_, value) values ('main', serverUrl', 'https://localhost:8443');
insert into config (group_, name_, value) values ('main', secureUrl', 'https://localhost:8444');
insert into config (group_, name_, value) values ('main', startH2TcpServer', 'true');
insert into config (group_, name_, value) values ('main', gtalk', 'default');
insert into config (group_, name_, value) values ('main', dataSource', 'h2_croc');
insert into config (group_, name_, value) values ('main', logLevel', 'INFO');
insert into config (group_, name_, value) values ('main', adminContact', 'evans');
insert into config (group_, name_, value) values ('main', httpServer', 'http8080');
insert into config (group_, name_, value) values ('main', publicHttpsServer', 'https8443');
insert into config (group_, name_, value) values ('main', privateHttpsServer', 'https8444auth');
insert into config (group_, name_, value) values ('main', printLog', 'false');
insert into config (group_, name_, value) values ('main', testPost', 'false');
insert into config (group_, name_, value) values ('main', testPostUrl', 'https://localhost:8443/post/aide/evans');
insert into config (group_, name_, value) values ('main', shutdownUrl', 'https://localhost:8444/shutdown');
insert into config (group_, name_, value) values ('main', terminate', '16s');
insert into config (group_, name_, value) values ('Gtalk.default', enabled', 'false');
insert into config (group_, name_, value) values ('Contact.evans', fullName', 'Evan Summers');
insert into config (group_, name_, value) values ('Contact.evans', email', 'evan.summers@gmail.com');
insert into config (group_, name_, value) values ('Contact.evans', im', 'evan.summers@gmail.com');
insert into config (group_, name_, value) values ('Contact.evans', enabled', 'true');
insert into config (group_, name_, value) values ('HttpServer.http8080', port', '8080');
insert into config (group_, name_, value) values ('HttpServer.http8080', enabled', 'true');
insert into config (group_, name_, value) values ('HttpsServer.https8443', port', '8443');
insert into config (group_, name_, value) values ('HttpsServer.https8443', enabled', 'true');
insert into config (group_, name_, value) values ('HttpsServer.https8443', clientAuth', 'false');
insert into config (group_, name_, value) values ('HttpsServer.https8444auth', port', '8444');
insert into config (group_, name_, value) values ('HttpsServer.https8444auth', enabled', 'true');
insert into config (group_, name_, value) values ('HttpsServer.https8444auth', clientAuth', 'true');
insert into config (group_, name_, value) values ('DataSource.h2_mem', driver', 'org.h2.Driver');
insert into config (group_, name_, value) values ('DataSource.h2_mem', url', 'jdbc:h2:mem');
insert into config (group_, name_, value) values ('DataSource.h2_mem', user', 'sa');
insert into config (group_, name_, value) values ('DataSource.h2_croc', driver', 'org.h2.Driver');
insert into config (group_, name_, value) values ('DataSource.h2_croc', url', 'jdbc:h2:tcp://localhost/~/croc');
insert into config (group_, name_, value) values ('DataSource.h2_croc', user', 'sa');
insert into config (group_, name_, value) values ('DataSource.h2_croc', enabled', 'true');
insert into config (group_, name_, value) values ('DataSource.pg_croc', driver', 'org.postgresql.Driver');
insert into config (group_, name_, value) values ('DataSource.pg_croc', url', 'postgresql://localhost/croc');
insert into config (group_, name_, value) values ('DataSource.pg_croc', user', 'croc');