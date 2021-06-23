DELETE
FROM case_prefill
where prefill_group = 'SEARCH_METADATA'
  and key in ('caseId', 'status', 'flowType');