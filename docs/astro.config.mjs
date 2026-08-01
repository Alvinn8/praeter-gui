// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	site: 'https://alvinn8.github.io',
	base: '/praeter-gui',
	integrations: [
		starlight({
			title: 'praeter-gui',
			description: 'Create vanilla-style GUIs with resource packs.',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/Alvinn8/praeter-gui' }],
			// customCss: ['./src/styles/custom.css'],
			sidebar: [
				{ label: 'Getting Started', link: '/getting-started/' },
				{
					label: 'Guides',
					items: [
                        { label: 'GUI Basics', link: '/guides/gui-basics/' },
					],
				},
				{
					label: 'Javadoc',
					items: [
						{ label: 'common', link: '/javadoc/common/', attrs: { target: '_blank' } },
						{ label: 'paper', link: '/javadoc/paper/', attrs: { target: '_blank' } },
						{ label: 'fabric', link: '/javadoc/fabric/', attrs: { target: '_blank' } },
					],
				},
			],
		}),
	],
});
