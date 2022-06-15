
INSERT INTO eeuser (id, nick, passwordhash, name, active, deleted, verified, forcepasswordchange, loginattempts) VALUES (1, 'admin','ARR5oS7UxDv5uwh6KwV++xK1LBDtg4SE+0EQOYFQiFJz/wEMSN0vTjeh388mvecXWP+Y5Cep3vxV8VRR4XF1FgSGPQvaFMQkihg1Zm1Avc5w2auD3UrcorhyZNuht7WvrU3eM4aSOnsetWf7gzhhQV9zZi5Xo9L3CDszvvwpIdblcFD7s9Waz66FWfb4oInqaj0nWciL6RNcv0MgUhMYYUmAV6RUvaG0w1Z7akES1SJBy5up9FUBW7lXH43T/nWtNSrMPsJ3lPerDLW7Gj2Z2wjk282kByzSseqkD2emneaq0roZM0ho4bAVE8CuMv/ygehuR1m4q6ckaNU6aduftMXMa7IVF4C8ZMUmUNwx9Uuf', 'User Administrator','Y','N','N','N', 0) ON CONFLICT(id) DO NOTHING;
INSERT INTO eeuser (id, nick, passwordhash, name, active, deleted, verified, forcepasswordchange, loginattempts) VALUES (2, 'guest','AZSVT7r0hGegL6+vMQuTLUdaCDJcd5vnzY4GBfdqg7EEKczUJgIiLGC9Qe1p2+tC3QVTWM3DkIruv44B/mN7gbNdT+o6jwxnjUHCzRKQibL4dEPT3ZNIa+uN0HtVbgo1LMRs009YkmszFcp9aQaeWQdwpKkCzKB9LEcDrC1i3aq33vbPaO0k/+SHOsdE4rmRWehGY7C38Te4sh0hA/EO8n4cH8811APojSOfqFdfpdEVvveTNZ6htfwvMphFT+z5YhjZ1wuOWUCf14iascMPV1S+kPsvAQqqQcZ1Jj/x3kAcD5H6OTbq9ReF4iSX3m0Q1azKBklKCoYCb85meGRgvpM0M5yEZ4M02+rg2JlVAsW8', 'Guest','Y','N','N','N', 0) ON CONFLICT(id) DO NOTHING;
INSERT INTO role (id, name) VALUES (3, 'admin') ON CONFLICT(id) DO NOTHING;
INSERT INTO role (id, name) VALUES (4, 'restapi') ON CONFLICT(id) DO NOTHING;
INSERT INTO role (id, name) VALUES (5, 'guest') ON CONFLICT(id) DO NOTHING;
INSERT INTO eeuser_roles (eeuser, role) VALUES (1, 3);
INSERT INTO eeuser_roles (eeuser, role) VALUES (2, 5);
INSERT INTO role_perms (role, name) VALUES (3, 'login');
INSERT INTO role_perms (role, name) VALUES (3, 'sysadmin');
INSERT INTO role_perms (role, name) VALUES (3, 'users.read');
INSERT INTO role_perms (role, name) VALUES (3, 'users.write');
INSERT INTO role_perms (role, name) VALUES (3, 'sysconfig.read');
INSERT INTO role_perms (role, name) VALUES (3, 'sysconfig.write');
INSERT INTO role_perms (role, name) VALUES (5, 'login');
INSERT INTO role_perms (role, name) VALUES (5, 'users.read');
INSERT INTO role_perms (role, name) VALUES (5, 'sysconfig.read');
INSERT INTO sysconfig (id, value, readperm, writeperm) VALUES ('perm.login',             'Login', 'sysconfig.read', null) ON CONFLICT(id) DO NOTHING;
INSERT INTO sysconfig (id, value, readperm, writeperm) VALUES ('perm.sysadmin',          'System Admin Permission', 'sysconfig.read', null) ON CONFLICT(id) DO NOTHING;
INSERT INTO sysconfig (id, value, readperm, writeperm) VALUES ('perm.sysconfig.read',    'Read SysConfig', 'sysconfig.read', null) ON CONFLICT(id) DO NOTHING;
INSERT INTO sysconfig (id, value, readperm, writeperm) VALUES ('perm.sysconfig.write',   'Edit SysConfig', 'sysconfig.read', null) ON CONFLICT(id) DO NOTHING;
INSERT INTO sysconfig (id, value, readperm, writeperm) VALUES ('perm.users.read',        'Read permission on Users', 'sysconfig.read', null) ON CONFLICT(id) DO NOTHING;
INSERT INTO sysconfig (id, value, readperm, writeperm) VALUES ('perm.users.write',       'Write permission on Users', 'sysconfig.read', null) ON CONFLICT(id) DO NOTHING;
INSERT INTO sysconfig (id, value, readperm, writeperm) VALUES ('remember_password_enabled', 'true', 'sysconfig.read', 'sysconfig.write') ON CONFLICT(id) DO NOTHING;
----
---- Data for Name: eeuser; Type: TABLE DATA; Schema: public; Owner: jpos
----
--
--COPY eeuser (id, nick, passwordhash, name, email, active, deleted, verified, forcepasswordchange, startdate, enddate) FROM stdin;
--1	admin	ARR5oS7UxDv5uwh6KwV++xK1LBDtg4SE+0EQOYFQiFJz/wEMSN0vTjeh388mvecXWP+Y5Cep3vxV8VRR4XF1FgSGPQvaFMQkihg1Zm1Avc5w2auD3UrcorhyZNuht7WvrU3eM4aSOnsetWf7gzhhQV9zZi5Xo9L3CDszvvwpIdblcFD7s9Waz66FWfb4oInqaj0nWciL6RNcv0MgUhMYYUmAV6RUvaG0w1Z7akES1SJBy5up9FUBW7lXH43T/nWtNSrMPsJ3lPerDLW7Gj2Z2wjk282kByzSseqkD2emneaq0roZM0ho4bAVE8CuMv/ygehuR1m4q6ckaNU6aduftMXMa7IVF4C8ZMUmUNwx9Uuf	User Administrator	\N	Y	N	N	N	\N	\N
--2	guest	AZSVT7r0hGegL6+vMQuTLUdaCDJcd5vnzY4GBfdqg7EEKczUJgIiLGC9Qe1p2+tC3QVTWM3DkIruv44B/mN7gbNdT+o6jwxnjUHCzRKQibL4dEPT3ZNIa+uN0HtVbgo1LMRs009YkmszFcp9aQaeWQdwpKkCzKB9LEcDrC1i3aq33vbPaO0k/+SHOsdE4rmRWehGY7C38Te4sh0hA/EO8n4cH8811APojSOfqFdfpdEVvveTNZ6htfwvMphFT+z5YhjZ1wuOWUCf14iascMPV1S+kPsvAQqqQcZ1Jj/x3kAcD5H6OTbq9ReF4iSX3m0Q1azKBklKCoYCb85meGRgvpM0M5yEZ4M02+rg2JlVAsW8	Guest	\N	Y	N	N	N	\N	\N
--\.


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: jpos
--

--COPY role (id, name) FROM stdin;
--3	admin
--4	restapi
--5	guest
--\.

--
-- Data for Name: eeuser_roles; Type: TABLE DATA; Schema: public; Owner: jpos
--

--COPY eeuser_roles (eeuser, role) FROM stdin;
--1	3
--2	5
--\.

--
-- Data for Name: role_perms; Type: TABLE DATA; Schema: public; Owner: jpos
--
--
--COPY role_perms (role, name) FROM stdin;
--3	login
--3	sysadmin
--3	users.read
--3	users.write
--3	sysconfig.read
--3	sysconfig.write
--5	login
--5	users.read
--5	sysconfig.read
--\.


--
-- Data for Name: sysconfig; Type: TABLE DATA; Schema: public; Owner: jpos
--

--COPY sysconfig (id, value, readperm, writeperm) FROM stdin;
--perm.login	Login	sysconfig.read	\N
--perm.syadmin	System Admin Permission	sysconfig.read	\N
--perm.sysconfig.read	Read System Configuration	sysconfig.read	\N
--perm.sysconfig.admin	Edit System Configuration	sysconfig.read	\N
--perm.users.read	Read permission on Users	sysconfig.read	\N
--perm.users.write	Write permission on Users	sysconfig.read	\N
--remember_password_enabled	true	sysconfig.read	sysconfig.write
--\.
