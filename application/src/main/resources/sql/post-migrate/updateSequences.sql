SELECT setval('rina.case_id_seq', (SELECT MAX(CAST(id AS Int)) FROM rina.rina_case), true);