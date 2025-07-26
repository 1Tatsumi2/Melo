using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MusicApp.Models
{
	public class SearchResultsViewModel
	{
		public SongViewModel TopResult { get; set; }
		public List<SongViewModel> Songs { get; set; }
		public List<SingerDetailViewModel> ArtistNames { get; set; } // Correctly defined as a list of SingerDetailViewModel
		public List<AlbumDetailViewModel> Albums { get; set; }
		public List<PlayListDetailViewModel> Playlists { get; set; }
	}

}