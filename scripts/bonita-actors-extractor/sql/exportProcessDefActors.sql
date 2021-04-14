select array_to_json(array_agg(row_to_json(t)))
from (
  select 
  split_part(pd.name, '_v', 1) as "processDefinition",
  split_part(pd.name, '_v', 2) as "version",
  (
      select array_to_json(array_agg(row_to_json(d)))
      from (
        select id::text, name
        from actor
        where
          scopeid=pd.processid
		    and
          name <> 'System'
        order by id, name asc
      ) d
    ) as actors
  from process_definition pd
   where
		(pd.name like 'PO_%_v4.1' OR pd.name like 'CP_%_v4.1' OR pd.name like 'PO_%_v4.2' OR pd.name like 'CP_%_v4.2')
	and 
		pd.activationstate = 'ENABLED'
) t