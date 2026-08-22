<script lang="ts">
	import type { MicrobiologyGramkleuringDto } from '$lib/types/dbTypes';
	import { createEventDispatcher } from 'svelte';

	export let gramkleuring: MicrobiologyGramkleuringDto;
	export let locked = false;

	const dispatch = createEventDispatcher<{ save: MicrobiologyGramkleuringDto }>();

	const VALID_SCORES = ['0', '+', '++', '+++', '++++'];

	function save() {
		dispatch('save', gramkleuring);
	}
</script>

<div class="space-y-6 p-4">
	<section>
		<h3 class="mb-2 font-semibold text-gray-700">Commentaar voor protocol</h3>
		<textarea
			bind:value={gramkleuring.commentaar}
			disabled={locked}
			on:blur={save}
			class="h-32 w-full rounded-lg border border-gray-400 bg-gray-200 p-3 disabled:cursor-not-allowed disabled:bg-gray-300"
			placeholder="Commentaar gramkleuring..."
		></textarea>
	</section>

	<section class="pt-6">
		<table class="w-full border-collapse">
			<thead>
				<tr class="border-b border-gray-300 text-left text-sm text-gray-500">
					<th class="w-1/4 p-2">Bepaling</th>
					<th class="w-1/4 p-2">Score</th>
					<th class="w-1/2 p-2">Commentaar</th>
				</tr>
			</thead>
			<tbody>
				{#each gramkleuring.rows as row, i (row.bepaling)}
					<tr class="border-b border-gray-200">
						<td class="p-2 font-semibold">{row.bepaling}</td>
						<td class="p-2">
							<select
								bind:value={row.score}
								disabled={locked}
								on:change={save}
								class="w-full rounded-lg border border-gray-400 bg-gray-200 px-2 py-1 disabled:bg-gray-300"
							>
								<option value="">—</option>
								{#each VALID_SCORES as score}
									<option value={score}>{score}</option>
								{/each}
							</select>
						</td>
						<td class="p-2">
							<input
								type="text"
								bind:value={row.commentaar}
								disabled={locked}
								on:blur={save}
								class="w-full rounded-lg border border-gray-400 bg-gray-200 px-2 py-1 disabled:bg-gray-300"
							/>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</section>
</div>
