// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	site: 'https://alvinn8.github.io',
	base: '/praeter-gui',
	trailingSlash: 'never',
	integrations: [
		starlight({
			title: 'praeter-gui',
			description: 'Create vanilla-style GUIs with resource packs.',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/Alvinn8/praeter-gui' }],
			// customCss: ['./src/styles/custom.css'],
			sidebar: [
				{
                    label: 'Getting Started',
                    items: [
                        { label: 'Installation', link: '/getting-started/installation' },
                        { label: 'GUI Basics', link: '/getting-started/gui-basics' },
                    ],
                },
				{
					label: 'Concepts',
					items: [

                        { label: 'Title', link: '/concepts/title' },
                        { label: 'Slots', link: '/concepts/slots' },
					],
				},
				{
					label: 'Javadoc',
					items: [
						{ label: 'common', link: '/javadoc/common', attrs: { target: '_blank' } },
						{ label: 'paper', link: '/javadoc/paper', attrs: { target: '_blank' } },
						{ label: 'fabric', link: '/javadoc/fabric', attrs: { target: '_blank' } },
					],
				},
			],
		}),
	],
});
