insert into router.providers (id, name, base_url, status)
values
  ('openai', 'OpenAI', 'https://api.openai.com/v1', 'ACTIVE'),
  ('anthropic', 'Anthropic', 'https://api.anthropic.com', 'ACTIVE')
on conflict (id) do nothing;

insert into router.models (provider_id, id, name, max_context_tokens, price_prompt_per_1k, price_completion_per_1k, capabilities)
values
  ('openai', 'gpt-5-mini', 'GPT-5 mini', 100000, 0.000215, 0.001718, null),
  ('openai', 'gpt-5.2', 'GPT-5.2', 100000, 0.001503, 0.012025, null),
  ('anthropic', 'claude-sonnet-4-5', 'Claude Sonnet 4.5', 100000, 0.002577, 0.012884, null)
on conflict (provider_id, id) do update
set
  price_prompt_per_1k = excluded.price_prompt_per_1k,
  price_completion_per_1k = excluded.price_completion_per_1k,
  updated_at = now();
