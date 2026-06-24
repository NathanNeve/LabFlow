<script lang="ts">
	import type { MicrobiologyAntibiogramEntryDto } from '$lib/types/dbTypes';
	import { createEventDispatcher } from 'svelte';

	export let entries: MicrobiologyAntibiogramEntryDto[] = [];
	export let locked = false;

	const dispatch = createEventDispatcher<{
		save: { antibioticaId: number; beoordeling: string };
	}>();

	function onChange(entry: MicrobiologyAntibiogramEntryDto) {
		dispatch('save', { antibioticaId: entry.antibioticaId, beoordeling: entry.beoordeling });
	}
</script>

<div class="overflow-y-auto p-4">
	<table class="w-full border-collapse">
		<thead>
			<tr class="border-b border-gray-300 text-left text-sm text-gray-500">
				<th class="w-1/2 p-2">Antibioticum</th>
				<th class="w-1/2 p-2">Beoordeling</th>
			</tr>
		</thead>
		<tbody>
			{#each entries as entry (entry.antibioticaId)}
				<tr class="border-b border-gray-200">
					<td class="p-2 font-medium">{entry.antibioticaNaam}</td>
					<td class="p-2">
						<select
							bind:value={entry.beoordeling}
							disabled={locked}
							on:change={() => onChange(entry)}
							class="w-full max-w-xs rounded-lg border border-gray-400 bg-gray-200 px-2 py-1 disabled:bg-gray-300"
						>
							<option value="R">R</option>
							<option value="S">S</option>
							<option value="I">I</option>
						</select>
					</td>
				</tr>
			{/each}
		</tbody>
	</table>
</div>
